package com.cdyt.be.service;

import com.cdyt.be.common.exception.BusinessException;
import com.cdyt.be.dto.auth.LoginRequestDto;
import com.cdyt.be.dto.auth.LoginResponseDto;
import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import com.cdyt.be.entity.UserToken;
import com.cdyt.be.repository.AuthRepository;
import com.cdyt.be.repository.RoleRepository;
import com.cdyt.be.repository.UserRepository;
import com.cdyt.be.util.JwtUtils;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthRepository authRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtUtils jwtUtils;
  private final PasswordEncoder passwordEncoder;

  @Value("${jwt.expiration-days:1}")
  private int normalExpirationDays;

  @Value("${jwt.expiration-remember-days:7}")
  private int rememberMeExpirationDays;

  @Transactional
  public LoginResponseDto login(LoginRequestDto req) {
    // Manual authentication - find user by email
    User user = userRepository.findByEmail(req.getEmail())
        .orElseThrow(() -> BusinessException.unauthorized("Invalid email or password"));

    // Check if user account is active
    if (!user.getIsActive()) {
      throw BusinessException.forbidden("Account is deactivated. Please contact administrator.");
    }

    // Manual password verification
    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw BusinessException.unauthorized("Invalid email or password");
    }

    // Optional: Check if user is verified (uncomment if needed)
    if (!user.getIsVerified()) {
      throw BusinessException.forbidden("Account is not verified. Please verify your email.");
    }

    // Generate JWT token
    String token = jwtUtils.generateToken(user);

    // Calculate database token expiration (this controls actual token lifecycle)
    int expirationDays = req.isRememberPassword() ? rememberMeExpirationDays : normalExpirationDays;
    LocalDateTime expirationDate = LocalDateTime.now().plusDays(expirationDays);

    // Save token to database
    UserToken userToken = createUserToken(user, token, expirationDate, req.isRememberPassword());
    authRepository.save(userToken);

    return new LoginResponseDto(token, userToken.getExpiredDate());
  }

  public LoginResponseDto register(CreateUserDto req) {
    // Check if user already exists
    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
      throw BusinessException.alreadyExists("User with email", req.getEmail());
    }

    // Create new user
    User user = new User();
    user.setFullName(req.getFullName());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setPhone(req.getPhone());
    user.setAddress(req.getAddress());
    user.setSex(req.getSex());
    user.setDob(req.getDob());
    user.setAvatar(req.getAvatar());
    user.setIsActive(true);
    user.setIsVerified(false);

    // Handle roles
    if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
      Set<Integer> roleIntIds = req.getRoleIds().stream()
          .map(Long::intValue)
          .collect(java.util.stream.Collectors.toSet());
      Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIntIds));
      user.setRole(roles);
    }

    // Save user
    User savedUser = userRepository.save(user);

    // Generate token for immediate login after registration
    String token = jwtUtils.generateToken(savedUser);

    // Save token
    UserToken userToken = new UserToken();
    userToken.setUser(savedUser);
    userToken.setToken(token);
    userToken.setExpiredDate(LocalDateTime.now().plusDays(1));
    userToken.setRememberPassword(false);
    authRepository.save(userToken);

    return new LoginResponseDto(token, userToken.getExpiredDate());
  }

  /**
   * Logout user by removing token from database
   */
  @Transactional
  public void logout(String token) {
    authRepository.findByToken(token)
        .ifPresent(authRepository::delete);
  }

  /**
   * Logout user from all devices
   */
  @Transactional
  public void logoutAllDevices(Long userId) {
    authRepository.deleteByUserId(userId);
  }

  /**
   * Cleanup expired tokens (can be called by scheduled task)
   */
  @Transactional
  public int cleanupExpiredTokens() {
    var expiredTokens = authRepository.findAll().stream()
        .filter(token -> token.getExpiredDate().isBefore(LocalDateTime.now()))
        .toList();

    authRepository.deleteAll(expiredTokens);
    return expiredTokens.size();
  }

  // Helper method
  private UserToken createUserToken(User user, String token, LocalDateTime expiration, boolean rememberPassword) {
    UserToken userToken = new UserToken();
    userToken.setUser(user);
    userToken.setToken(token);
    userToken.setExpiredDate(expiration);
    userToken.setRememberPassword(rememberPassword);
    return userToken;
  }
}
