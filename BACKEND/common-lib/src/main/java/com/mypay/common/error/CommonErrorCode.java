package com.mypay.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    VALIDATION_FAILED("COMMON", "Validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("COMMON", "Malformed request body", HttpStatus.BAD_REQUEST),
    MISSING_HEADER("COMMON", "Missing required request header", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON", "Authentication is required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON", "Access denied", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("COMMON", "Resource not found", HttpStatus.NOT_FOUND),
    CONFLICT("COMMON", "The request conflicts with existing data", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION("COMMON", "The request conflicts with existing data", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED("COMMON", "HTTP method is not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_ERROR("COMMON", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String module;
    private final String defaultMessage;
    private final HttpStatus status;

    @Override
    public String code() {
        return name();
    }

    @Override
    public String module() {
        return module;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
