package com.cdyt.be.service;

import com.cdyt.be.common.exception.BusinessException;
import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.dto.user.UpdateUserDto;
import com.cdyt.be.dto.user.UserResponseDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import com.cdyt.be.mapper.UserMapper;
import com.cdyt.be.repository.RoleRepository;
import com.cdyt.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserResponseDto createUser(CreateUserDto createUserDto) {
    // Check if email already exists
    if (userRepository.existsByEmail(createUserDto.getEmail())) {
      throw BusinessException.alreadyExists("User with email", createUserDto.getEmail());
    }

    // Use MapStruct to map DTO to Entity
    User user = userMapper.createDtoToEntity(createUserDto);
    user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));

    // Set roles if provided
    if (createUserDto.getRoleIds() != null && !createUserDto.getRoleIds().isEmpty()) {
      Set<Role> roles = new HashSet<>();
      for (Long roleId : createUserDto.getRoleIds()) {
        roleRepository.findById(Math.toIntExact(roleId))
            .ifPresent(roles::add);
      }
      user.setRole(roles);
    }

    User savedUser = userRepository.save(user);
    return userMapper.entityToResponseDto(savedUser);
  }

  public Optional<UserResponseDto> getUserById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::entityToResponseDto);
  }

  public UserResponseDto getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::entityToResponseDto)
        .orElseThrow(() -> BusinessException.notFound("User with email", email));
  }

  public Page<UserResponseDto> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable)
        .map(userMapper::entityToResponseDto);
  }

  public List<UserResponseDto> getAllActiveUsers() {
    return userRepository.findByIsActiveTrueAndIsDeletedFalse()
        .stream()
        .map(userMapper::entityToResponseDto)
        .collect(Collectors.toList());
  }

  public UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BusinessException.notFound("User", id));

    // Use MapStruct to update entity from DTO
    userMapper.updateEntityFromDto(updateUserDto, user);

    // Update password if provided
    if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
    }

    // Update roles if provided
    if (updateUserDto.getRoleIds() != null) {
      Set<Role> roles = new HashSet<>();
      for (Long roleId : updateUserDto.getRoleIds()) {
        roleRepository.findById(Math.toIntExact(roleId))
            .ifPresent(roles::add);
      }
      user.setRole(roles);
    }

    User savedUser = userRepository.save(user);
    return userMapper.entityToResponseDto(savedUser);
  }

  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BusinessException.notFound("User", id));

    user.setIsDeleted(true);
    userRepository.save(user);
  }

  public void activateUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BusinessException.notFound("User", id));

    user.setIsActive(true);
    userRepository.save(user);
  }

  public void deactivateUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BusinessException.notFound("User", id));

    user.setIsActive(false);
    userRepository.save(user);
  }

  public void verifyUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BusinessException.notFound("User", id));

    user.setIsVerified(true);
    userRepository.save(user);
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        Boolean.TRUE.equals(user.getIsActive()), true, true, true,
        user.getRole().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
            .collect(Collectors.toList()));
  }
}
