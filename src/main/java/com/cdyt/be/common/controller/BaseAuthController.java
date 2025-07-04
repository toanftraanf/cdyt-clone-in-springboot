package com.cdyt.be.common.controller;

import com.cdyt.be.common.context.UserContextHolder;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Getter
@Setter
public abstract class BaseAuthController {

  /**
   * Current authenticated user
   */
  protected User currentUser;

  /**
   * Authentication result
   */
  protected AuthResult authResult;

  /**
   * Client IP Address
   */
  protected String ipAddress;

  /**
   * Check if current user is owner of a resource
   */
  protected boolean isOwner = false;

  /**
   * Get current authenticated user
   */
  public User getCurrentUser() {
    return UserContextHolder.getCurrentUser();
  }

  /**
   * Get client IP address
   */
  public String getClientIpAddress() {
    if (ipAddress != null) {
      return ipAddress;
    }

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
        .currentRequestAttributes();
    HttpServletRequest request = attributes.getRequest();

    // Try X-Forwarded-For header first (like your .NET code)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      ipAddress = xForwardedFor.split(",")[0].trim();
    } else {
      ipAddress = request.getRemoteAddr();
    }

    return ipAddress;
  }

  // ========== CONVENIENT RESPONSE METHODS ==========

  /**
   * Create successful response with automatic user context
   */
  protected <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
    return ResponseEntity.ok(ResponseUtils.success(data, message));
  }

  /**
   * Create successful response with automatic user context and additional
   * metadata
   */
  protected <T> ResponseEntity<ApiResponse<T>> ok(T data, String message,
      Map<String, Object> additionalMetadata) {
    return ResponseEntity.ok(ResponseUtils.success(data, message, additionalMetadata));
  }

  /**
   * Create created response (201) with automatic user context
   */
  protected <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtils.created(data, message));
  }

  /**
   * Create created response with automatic user context and additional metadata
   */
  protected <T> ResponseEntity<ApiResponse<T>> created(T data, String message,
      Map<String, Object> additionalMetadata) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseUtils.created(data, message, additionalMetadata));
  }

  /**
   * Create not found response (404) with automatic user context
   */
  protected <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseUtils.notFound(message));
  }

  /**
   * Create forbidden response (403) with automatic user context
   */
  protected <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseUtils.forbidden(message));
  }

  /**
   * Create bad request response (400) with automatic user context
   */
  protected <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtils.badRequest(message));
  }

  /**
   * Authentication result class
   */
  @Getter
  @Setter
  public static class AuthResult {

    private boolean isOk;
    private String message;
    private int statusCode;

    public AuthResult(boolean isOk, String message, int statusCode) {
      this.isOk = isOk;
      this.message = message;
      this.statusCode = statusCode;
    }

    public static AuthResult success() {
      return new AuthResult(true, "Success", 200);
    }

    public static AuthResult failure(String message, int statusCode) {
      return new AuthResult(false, message, statusCode);
    }
  }
}