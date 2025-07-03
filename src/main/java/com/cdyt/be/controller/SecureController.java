package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Example of how to use the authentication system
 * Similar to your .NET controller with [AuthenPermission] attribute
 */
@RestController
@RequestMapping("/api/secure")
@RequireAuth // This annotation protects the entire controller (like [AuthenPermission] in
             // .NET)
@Tag(name = "Secure API", description = "Protected endpoints that require authentication")
public class SecureController extends BaseAuthController { // Inherit from BaseAuthController (like Authentication in
                                                           // .NET)

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Returns authenticated user's profile information")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    public ResponseEntity<Map<String, Object>> getProfile() {
        // Access current user (equivalent to userToken in .NET)
        User user = getCurrentUser();

        return ResponseEntity.ok(Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "isActive", user.getIsActive()),
                "authResult", Map.of(
                        "isOk", authResult.isOk(),
                        "message", authResult.getMessage()),
                "clientIp", getClientIpAddress() // Access client IP (like your .NET implementation)
        ));
    }

    @PostMapping("/data")
    @Operation(summary = "Post secure data", description = "Example endpoint that requires authentication")
    public ResponseEntity<Map<String, Object>> postSecureData(@RequestBody Map<String, Object> data) {
        // You have access to all user context here
        User currentUser = getCurrentUser();
        String clientIp = getClientIpAddress();

        return ResponseEntity.ok(Map.of(
                "message", "Data received successfully",
                "processedBy", currentUser.getFullName(),
                "fromIp", clientIp,
                "receivedData", data));
    }

    @GetMapping("/check-owner/{resourceId}")
    @Operation(summary = "Check resource ownership", description = "Example of checking if user owns a resource")
    public ResponseEntity<Map<String, Object>> checkOwnership(@PathVariable Long resourceId) {
        User currentUser = getCurrentUser();

        // Example ownership check logic (implement based on your business rules)
        boolean isOwner = resourceId.equals(currentUser.getId()); // Simple example
        setOwner(isOwner); // Set owner flag (like checkIsOwner in .NET)

        return ResponseEntity.ok(Map.of(
                "resourceId", resourceId,
                "userId", currentUser.getId(),
                "isOwner", isOwner,
                "message", isOwner ? "You own this resource" : "You don't own this resource"));
    }
}