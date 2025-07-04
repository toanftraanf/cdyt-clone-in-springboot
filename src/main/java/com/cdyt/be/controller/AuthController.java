package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.dto.auth.LoginRequestDto;
import com.cdyt.be.dto.auth.LoginResponseDto;
import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
public class AuthController extends BaseAuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
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
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration request"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
  })
  public ResponseEntity<LoginResponseDto> register(@RequestBody @Valid CreateUserDto req) {
    LoginResponseDto response = authService.register(req);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @RequireAuth
  @Operation(summary = "User Logout", description = "Logout user from current device by invalidating the current token")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);

    if (token == null) {
      return badRequest("No token provided");
    }

    authService.logout(token);

    return ok(null, "Logged out successfully from current device");
  }

  @PostMapping("/logout-all")
  @RequireAuth
  @Operation(summary = "Logout All Devices", description = "Logout user from all devices by invalidating all tokens for the user")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> logoutAllDevices() {
    Long userId = getCurrentUser().getId();
    authService.logoutAllDevices(userId);

    return ok(null, "Logged out successfully from all devices");
  }

  /**
   * Extract JWT token from Authorization header
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
