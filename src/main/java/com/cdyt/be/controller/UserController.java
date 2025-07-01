package com.cdyt.be.controller;

import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.entity.User;
import com.cdyt.be.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "APIs for managing users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/save")
  public User save(@RequestBody @Valid CreateUserDto user) {
    return userService.save(user);
  }
}
