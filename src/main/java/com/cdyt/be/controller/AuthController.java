package com.cdyt.be.controller;

import com.cdyt.be.dto.auth.LoginRequestDto;
import com.cdyt.be.dto.auth.LoginResponseDto;
import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login successful"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid login request"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
  })
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto req) {
    LoginResponseDto response = authService.login(req);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register")
  @Operation(summary = "User Registration", description = "Register a new user and return JWT token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Registration successful"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration request"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
  })
  public ResponseEntity<LoginResponseDto> register(@RequestBody @Valid CreateUserDto req) {
    LoginResponseDto response = authService.register(req);
    return ResponseEntity.ok(response);
  }
}
