package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.dto.user.UpdateUserDto;
import com.cdyt.be.dto.user.UserResponseDto;
import com.cdyt.be.entity.User;
import com.cdyt.be.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for managing users")
public class UserController extends BaseAuthController {

  private final UserService userService;

  @PostMapping
  @Operation(summary = "Create a new user", description = "Creates a new user with the provided information")
  public ResponseEntity<Map<String, Object>> createUser(
      @Valid @RequestBody CreateUserDto createUserDto) {
    UserResponseDto createdUser = userService.createUser(createUserDto);

    // Using the new approach - no need to manually get user context!
    // return created(createdUser, "User created successfully");

    // For now, keeping the old format
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
        "user", createdUser,
        "message", "User created successfully"));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique identifier")
  public ResponseEntity<Map<String, Object>> getUserById(
      @Parameter(description = "User ID") @PathVariable Long id) {

    // Access current authenticated user (like userToken in .NET)
    User currentUser = getCurrentUser();
    String clientIp = getClientIpAddress();

    // Check ownership (like checkIsOwner in .NET)
    boolean isOwner = id.equals(currentUser.getId());
    setOwner(isOwner);

    return userService.getUserById(id)
        .map(user -> ResponseEntity.ok(Map.of(
            "user", user,
            "requestedBy", currentUser.getFullName(),
            "isOwner", isOwner,
            "clientIp", clientIp,
            "canEdit", isOwner, // Example permission logic
            "message", isOwner ? "Viewing your own profile" : "Viewing another user's profile")))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/email/{email}")
  @Operation(summary = "Get user by email", description = "Retrieves a user by their email address")
  public ResponseEntity<UserResponseDto> getUserByEmail(
      @Parameter(description = "User email") @PathVariable String email) {
    try {
      UserResponseDto user = userService.getUserByEmail(email);
      return ResponseEntity.ok(user);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  @Operation(summary = "Get all users with pagination", description = "Retrieves all users with pagination support")
  public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
    Page<UserResponseDto> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/active")
  @Operation(summary = "Get all active users", description = "Retrieves all active and non-deleted users")
  public ResponseEntity<List<UserResponseDto>> getAllActiveUsers() {
    List<UserResponseDto> activeUsers = userService.getAllActiveUsers();
    return ResponseEntity.ok(activeUsers);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Updates an existing user's information")
  public ResponseEntity<Map<String, Object>> updateUser(
      @Parameter(description = "User ID") @PathVariable Long id,
      @Valid @RequestBody UpdateUserDto updateUserDto) {
    try {
      // Access current authenticated user (like userToken in .NET)
      User currentUser = getCurrentUser();
      String clientIp = getClientIpAddress();

      // Check ownership/permission (like your .NET permission logic)
      boolean isOwner = id.equals(currentUser.getId());
      boolean hasAdminRole = currentUser.getRole().stream()
          .anyMatch(role -> "ADMIN".equals(role.getRoleName()));

      if (!isOwner && !hasAdminRole) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "Status", 1,
            "StatusCode", 403,
            "Object", "You can only update your own profile or need admin privileges",
            "isOk", false,
            "isError", true,
            "requestedBy", currentUser.getFullName(),
            "clientIp", clientIp));
      }

      UserResponseDto updatedUser = userService.updateUser(id, updateUserDto);

      return ResponseEntity.ok(Map.of(
          "user", updatedUser,
          "updatedBy", currentUser.getFullName(),
          "updatedAt", java.time.LocalDateTime.now(),
          "isOwner", isOwner,
          "hasAdminRole", hasAdminRole,
          "clientIp", clientIp,
          "message", "User updated successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Soft deletes a user (marks as deleted)")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User ID") @PathVariable Long id) {
    try {
      userService.deleteUser(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PatchMapping("/{id}/activate")
  @Operation(summary = "Activate user", description = "Activates a user account")
  public ResponseEntity<Void> activateUser(
      @Parameter(description = "User ID") @PathVariable Long id) {
    try {
      userService.activateUser(id);
      return ResponseEntity.ok().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PatchMapping("/{id}/deactivate")
  @Operation(summary = "Deactivate user", description = "Deactivates a user account")
  public ResponseEntity<Void> deactivateUser(
      @Parameter(description = "User ID") @PathVariable Long id) {
    try {
      userService.deactivateUser(id);
      return ResponseEntity.ok().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PatchMapping("/{id}/verify")
  @Operation(summary = "Verify user", description = "Verifies a user account")
  public ResponseEntity<Void> verifyUser(
      @Parameter(description = "User ID") @PathVariable Long id) {
    try {
      userService.verifyUser(id);
      return ResponseEntity.ok().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user info", description = "Returns complete context of authenticated user")
  public ResponseEntity<Map<String, Object>> getCurrentUserContext() {
    // Access all available context (like your .NET Authentication class provides)
    User currentUser = getCurrentUser();
    String clientIp = getClientIpAddress();
    AuthResult authResult = getAuthResult();
    boolean isOwner = this.isOwner; // Access the isOwner field directly

    // Demonstrate full context access (equivalent to your .NET userToken,
    // ResultCheckToken, etc.)
    return ResponseEntity.ok(Map.of(
        "user", Map.of(
            "id", currentUser.getId(),
            "fullName", currentUser.getFullName(),
            "email", currentUser.getEmail(),
            "isActive", currentUser.getIsActive(),
            "isVerified", currentUser.getIsVerified(),
            "roles", currentUser.getRole().stream()
                .map(role -> role.getRoleName())
                .toList()),
        "authenticationResult", Map.of(
            "isOk", authResult.isOk(),
            "message", authResult.getMessage(),
            "statusCode", authResult.getStatusCode()),
        "requestContext", Map.of(
            "clientIp", clientIp,
            "isOwner", isOwner,
            "timestamp", java.time.LocalDateTime.now(),
            "requestPath", "/api/users/me"),
        "permissions", Map.of(
            "canCreateUser", true,
            "canEditOwnProfile", true,
            "canEditOthers", currentUser.getRole().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleName())))));
  }
}
