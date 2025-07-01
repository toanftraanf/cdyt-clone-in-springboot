package com.cdyt.be.service;

import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.entity.User;
import com.cdyt.be.repository.UserRepositoty;
import com.cdyt.be.util.TextUtils;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepositoty userRepositoty;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepositoty userRepositoty, PasswordEncoder passwordEncoder) {
    this.userRepositoty = userRepositoty;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> getAllUsers() {
    return userRepositoty.findAll();
  }

  public User save(CreateUserDto user) {
    if (user.getId() != 0) {
      // If the user already exists, update it
      User existingUser = userRepositoty.findById(user.getId())
          .orElseThrow(() -> new IllegalArgumentException("User not found"));
      return getUser(user, existingUser);
    }
    User newUser = new User();
    return getUser(user, newUser);
  }

  private User getUser(CreateUserDto user, User newUser) {
    newUser.setEmail(user.getEmail());
    newUser.setIsActive(user.isActive());
    newUser.setFullName(user.getFullName());
    newUser.setPhone(user.getPhone());
    if (!TextUtils.isEmpty(user.getPassword())) {
      // Only update password if it is provided
      newUser.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    newUser.setAddress(user.getAddress());
    return userRepositoty.save(newUser);
  }
}
