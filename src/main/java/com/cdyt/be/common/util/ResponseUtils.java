package com.cdyt.be.common.util;

import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.context.UserContextHolder;
import com.cdyt.be.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for creating standardized API responses with user context
 * Provides convenient methods for controllers to create consistent responses
 */
public class ResponseUtils {

    // ========== AUTO-CONTEXT METHODS (No manual user/IP passing required)
    // ==========

    /**
     * Create a successful response with automatically fetched user context
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message)
                .withMetadata(createAutoUserContext());
    }

    /**
     * Create a successful response with auto user context and additional metadata
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = createAutoUserContext();
        if (additionalMetadata != null) {
            metadata.putAll(additionalMetadata);
        }
        return ApiResponse.success(data, message, metadata);
    }

    /**
     * Create a created response (201) with automatically fetched user context
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.created(data, message)
                .withMetadata(createAutoUserContext());
    }

    /**
     * Create a created response with auto user context and additional metadata
     */
    public static <T> ApiResponse<T> created(T data, String message, Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = createAutoUserContext();
        if (additionalMetadata != null) {
            metadata.putAll(additionalMetadata);
        }
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(201)
                .data(data)
                .message(message)
                .metadata(metadata)
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Create a not found response with automatically fetched user context
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>notFound(message)
                .withMetadata(createAutoUserContext());
    }

    /**
     * Create a forbidden response with automatically fetched user context
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>forbidden(message)
                .withMetadata(createAutoUserContext());
    }

    /**
     * Create a bad request response with automatically fetched user context
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>badRequest(message)
                .withMetadata(createAutoUserContext());
    }

    // ========== MANUAL CONTEXT METHODS (For backward compatibility) ==========

    /**
     * Create a successful response with user context metadata
     */
    public static <T> ApiResponse<T> success(T data, String message, User currentUser, String clientIp) {
        return ApiResponse.success(data, message)
                .withMetadata(createUserContext(currentUser, clientIp));
    }

    /**
     * Create a successful response with user context and additional metadata
     */
    public static <T> ApiResponse<T> success(T data, String message, User currentUser, String clientIp,
            Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = createUserContext(currentUser, clientIp);
        if (additionalMetadata != null) {
            metadata.putAll(additionalMetadata);
        }
        return ApiResponse.success(data, message, metadata);
    }

    /**
     * Create a created response (201) with user context metadata
     */
    public static <T> ApiResponse<T> created(T data, String message, User currentUser, String clientIp) {
        return ApiResponse.created(data, message)
                .withMetadata(createUserContext(currentUser, clientIp));
    }

    /**
     * Create a created response with user context and additional metadata
     */
    public static <T> ApiResponse<T> created(T data, String message, User currentUser, String clientIp,
            Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = createUserContext(currentUser, clientIp);
        if (additionalMetadata != null) {
            metadata.putAll(additionalMetadata);
        }
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(201)
                .data(data)
                .message(message)
                .metadata(metadata)
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Create a not found response with user context
     */
    public static <T> ApiResponse<T> notFound(String message, User currentUser, String clientIp) {
        return ApiResponse.<T>notFound(message)
                .withMetadata(createUserContext(currentUser, clientIp));
    }

    /**
     * Create a forbidden response with user context
     */
    public static <T> ApiResponse<T> forbidden(String message, User currentUser, String clientIp) {
        return ApiResponse.<T>forbidden(message)
                .withMetadata(createUserContext(currentUser, clientIp));
    }

    /**
     * Create a bad request response with user context
     */
    public static <T> ApiResponse<T> badRequest(String message, User currentUser, String clientIp) {
        return ApiResponse.<T>badRequest(message)
                .withMetadata(createUserContext(currentUser, clientIp));
    }

    /**
     * Create user context metadata automatically from current request context
     */
    private static Map<String, Object> createAutoUserContext() {
        User currentUser = getCurrentUserFromContext();
        String clientIp = getClientIpFromRequest();
        return createUserContext(currentUser, clientIp);
    }

    /**
     * Create standard user context metadata
     */
    private static Map<String, Object> createUserContext(User currentUser, String clientIp) {
        Map<String, Object> metadata = new HashMap<>();

        if (currentUser != null) {
            metadata.put("requestedBy", currentUser.getFullName());
            metadata.put("userId", currentUser.getId());
            metadata.put("userEmail", currentUser.getEmail());
        }

        if (clientIp != null) {
            metadata.put("clientIp", clientIp);
        }

        return metadata;
    }

    /**
     * Get current user from UserContextHolder (set by AuthenticationAspect)
     */
    private static User getCurrentUserFromContext() {
        try {
            return UserContextHolder.getCurrentUser();
        } catch (Exception e) {
            // Return null if no user context available (e.g., public endpoints)
            return null;
        }
    }

    /**
     * Get client IP address from current HTTP request
     */
    private static String getClientIpFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // Try X-Forwarded-For header first
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            // Return null if no request context available
            return null;
        }
    }

    /**
     * Helper method to add operation-specific metadata
     */
    public static Map<String, Object> operationMetadata(String operation, Object identifier) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", operation);
        if (identifier != null) {
            metadata.put("resourceId", identifier);
        }
        return metadata;
    }

    /**
     * Helper method to add count metadata for list operations
     */
    public static Map<String, Object> listMetadata(int totalCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", totalCount);
        return metadata;
    }

    /**
     * Helper method to add ownership metadata
     */
    public static Map<String, Object> ownershipMetadata(boolean isOwner) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("isOwner", isOwner);
        return metadata;
    }
}