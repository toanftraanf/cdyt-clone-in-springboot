package com.cdyt.be.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized API Response wrapper for consistent response format across all
 * endpoints
 * Based on the pattern from AuthenticationAspect but made more flexible and
 * reusable
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Response status (0 = success, 1 = error/failure)
     */
    @Builder.Default
    private Integer status = 0;

    /**
     * HTTP status code
     */
    private Integer statusCode;

    /**
     * Main response data/object
     */
    private T data;

    /**
     * Response message
     */
    private String message;

    /**
     * Whether the operation was successful
     */
    @Builder.Default
    private Boolean isOk = true;

    /**
     * Whether there was an error
     */
    @Builder.Default
    private Boolean isError = false;

    /**
     * Timestamp of the response
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Additional metadata (user info, client IP, etc.)
     */
    private Map<String, Object> metadata;

    // Static factory methods for common response types

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(200)
                .data(data)
                .message("Success")
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Create a successful response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(200)
                .data(data)
                .message(message)
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Create a successful response with data, message and metadata
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(200)
                .data(data)
                .message(message)
                .metadata(metadata)
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return ApiResponse.<T>builder()
                .status(1)
                .statusCode(statusCode)
                .message(message)
                .isOk(false)
                .isError(true)
                .build();
    }

    /**
     * Create an error response with metadata
     */
    public static <T> ApiResponse<T> error(int statusCode, String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .status(1)
                .statusCode(statusCode)
                .message(message)
                .metadata(metadata)
                .isOk(false)
                .isError(true)
                .build();
    }

    /**
     * Create unauthorized response (401)
     */
    public static <T> ApiResponse<T> unauthorized() {
        return error(401, "Authentication required");
    }

    /**
     * Create forbidden response (403)
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * Create not found response (404)
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * Create bad request response (400)
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * Create created response (201)
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status(0)
                .statusCode(201)
                .data(data)
                .message(message)
                .isOk(true)
                .isError(false)
                .build();
    }

    /**
     * Add metadata to existing response
     */
    public ApiResponse<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Add multiple metadata entries
     */
    public ApiResponse<T> withMetadata(Map<String, Object> metadata) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.putAll(metadata);
        return this;
    }
}