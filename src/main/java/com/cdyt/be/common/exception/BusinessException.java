package com.cdyt.be.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom business exception for application-specific errors
 * This exception includes HTTP status code and additional metadata
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_ERROR";
        this.data = null;
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = "BUSINESS_ERROR";
        this.data = null;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(String message, HttpStatus httpStatus, String errorCode, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.data = data;
    }

    // Static factory methods for common scenarios
    public static BusinessException notFound(String resource) {
        return new BusinessException(resource + " not found", HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public static BusinessException notFound(String resource, Object id) {
        return new BusinessException(resource + " not found with ID: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                id);
    }

    public static BusinessException alreadyExists(String resource) {
        return new BusinessException(resource + " already exists", HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }

    public static BusinessException alreadyExists(String resource, Object identifier) {
        return new BusinessException(resource + " already exists: " + identifier, HttpStatus.CONFLICT,
                "RESOURCE_ALREADY_EXISTS", identifier);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public static BusinessException invalidInput(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    public static BusinessException invalidState(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT, "INVALID_STATE");
    }
}