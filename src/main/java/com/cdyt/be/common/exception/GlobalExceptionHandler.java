package com.cdyt.be.common.exception;

import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that catches all exceptions and returns them
 * in the standard ApiResponse format with proper HTTP status codes
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    // ========== BUSINESS EXCEPTIONS ==========

    /**
     * Handle custom business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata(ex.getErrorCode(), ex.getData());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getHttpStatus().value(),
                ex.getMessage(),
                errorMetadata);

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // ========== VALIDATION EXCEPTIONS ==========

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing + "; " + replacement));

        Map<String, Object> errorMetadata = createErrorMetadata("VALIDATION_ERROR", fieldErrors);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed: " + fieldErrors.size() + " error(s)",
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement));

        Map<String, Object> errorMetadata = createErrorMetadata("CONSTRAINT_VIOLATION", violations);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint violations: " + violations.size() + " error(s)",
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ========== AUTHENTICATION & AUTHORIZATION EXCEPTIONS ==========

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("AUTHENTICATION_ERROR", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed: " + ex.getMessage(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle bad credentials specifically
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("BAD_CREDENTIALS", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials",
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("ACCESS_DENIED", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "Access denied: " + ex.getMessage(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ========== DATA EXCEPTIONS ==========

    /**
     * Handle data integrity violations (duplicate keys, foreign key constraints,
     * etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("duplicate key")) {
                message = "Duplicate entry - resource already exists";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Referenced resource does not exist";
            } else if (ex.getMessage().contains("not-null")) {
                message = "Required field is missing";
            }
        }

        Map<String, Object> errorMetadata = createErrorMetadata("DATA_INTEGRITY_ERROR", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                message,
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // ========== WEB EXCEPTIONS ==========

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("Malformed request: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("MALFORMED_REQUEST", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed request body",
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("Missing parameter: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("MISSING_PARAMETER", ex.getParameterName());

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Missing required parameter: " + ex.getParameterName(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter {}: {}", ex.getName(), ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("TYPE_MISMATCH",
                Map.of("parameter", ex.getName(), "expectedType", ex.getRequiredType().getSimpleName()));

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid parameter type for: " + ex.getName(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("METHOD_NOT_SUPPORTED",
                Map.of("method", ex.getMethod(), "supportedMethods", ex.getSupportedMethods()));

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "HTTP method not supported: " + ex.getMethod(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle 404 - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found: {}", ex.getMessage());

        Map<String, Object> errorMetadata = createErrorMetadata("ENDPOINT_NOT_FOUND",
                Map.of("path", ex.getRequestURL(), "method", ex.getHttpMethod()));

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                "Endpoint not found: " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // ========== GENERIC EXCEPTIONS ==========

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorMetadata = createErrorMetadata("RUNTIME_ERROR", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorMetadata = createErrorMetadata("INTERNAL_ERROR", null);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An internal error occurred",
                errorMetadata);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== HELPER METHODS ==========

    /**
     * Create error metadata with automatic user context
     */
    private Map<String, Object> createErrorMetadata(String errorCode, Object errorData) {
        Map<String, Object> metadata = new HashMap<>();

        // Add error-specific information
        metadata.put("errorCode", errorCode);
        metadata.put("timestamp", LocalDateTime.now());

        if (errorData != null) {
            metadata.put("errorData", errorData);
        }

        // Add user context automatically
        try {
            com.cdyt.be.entity.User currentUser = com.cdyt.be.common.context.UserContextHolder.getCurrentUser();
            if (currentUser != null) {
                metadata.put("requestedBy", currentUser.getFullName());
                metadata.put("userId", currentUser.getId());
                metadata.put("userEmail", currentUser.getEmail());
            }

            // Get client IP from request
            org.springframework.web.context.request.ServletRequestAttributes attributes = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes();
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                String clientIp = (xForwardedFor != null && !xForwardedFor.isEmpty())
                        ? xForwardedFor.split(",")[0].trim()
                        : request.getRemoteAddr();
                metadata.put("clientIp", clientIp);
            }
        } catch (Exception e) {
            // Ignore if unable to get user context
            log.debug("Unable to get user context for error response: {}", e.getMessage());
        }

        return metadata;
    }
}