package com.mypay.common.exception;

import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.dto.ApiResponse;
import com.mypay.common.error.CommonErrorCode;
import com.mypay.common.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("Application exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : commonCodeFor(ex.getStatus());
        return ResponseEntity.status(ex.getStatus()).body(error(errorCode, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(error(CommonErrorCode.VALIDATION_FAILED, "Validation failed: " + errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(error(CommonErrorCode.VALIDATION_FAILED, "Validation failed: " + errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(error(CommonErrorCode.MALFORMED_REQUEST));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Missing request header: {}", ex.getHeaderName());
        return ResponseEntity.badRequest().body(error(CommonErrorCode.MISSING_HEADER));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(error(CommonErrorCode.VALIDATION_FAILED, "Invalid value for parameter '" + ex.getName() + "'"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error(CommonErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        log.warn("Data integrity violation: {}", rootMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(CommonErrorCode.DATA_INTEGRITY_VIOLATION));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(error(CommonErrorCode.INTERNAL_ERROR));
    }

    private ApiResponse<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.defaultMessage());
    }

    private ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return ApiResponse.error(message, errorCode.code(), errorCode.module(), RequestContextHolder.traceId());
    }

    private ErrorCode commonCodeFor(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) return CommonErrorCode.UNAUTHORIZED;
        if (status == HttpStatus.FORBIDDEN) return CommonErrorCode.FORBIDDEN;
        if (status == HttpStatus.NOT_FOUND) return CommonErrorCode.RESOURCE_NOT_FOUND;
        if (status == HttpStatus.CONFLICT) return CommonErrorCode.CONFLICT;
        if (status == HttpStatus.BAD_REQUEST) return CommonErrorCode.VALIDATION_FAILED;
        return CommonErrorCode.INTERNAL_ERROR;
    }
}
