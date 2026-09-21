package com.likhith.bankingapi.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.likhith.bankingapi.common.ApiErrorResponse;
import com.likhith.bankingapi.common.FieldErrorDetail;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("Validation failed: {}", fieldErrors);
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.name(), "VALIDATION_FAILED",
                "Request validation failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("Constraint violation: {}", fieldErrors);
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.name(), "VALIDATION_FAILED",
                "Request validation failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.name(), "MALFORMED_REQUEST",
                "Request body is missing or malformed", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch on parameter {}: {}", ex.getName(), ex.getMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.name(), "INVALID_PARAMETER",
                "Parameter '" + ex.getName() + "' has an invalid value", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleViolationException ex) {
        log.warn("Business rule violation [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.name(), ex.getErrorCode(),
                ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.NOT_FOUND.name(), ex.getErrorCode(),
                ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateIdempotencyKey(DuplicateIdempotencyKeyException ex) {
        log.warn("Duplicate idempotency key rejected: {}", ex.getMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.CONFLICT.name(), "DUPLICATE_IDEMPOTENCY_KEY",
                ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), "INTERNAL_ERROR",
                "An unexpected error occurred", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private FieldErrorDetail toFieldError(FieldError fieldError) {
        return new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private FieldErrorDetail toFieldError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        return new FieldErrorDetail(path, violation.getMessage());
    }
}
