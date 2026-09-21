package com.likhith.contractforge.exception;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OpenApiFetchException.class)
    public ResponseEntity<ErrorResponse> handleFetchFailure(OpenApiFetchException ex) {
        log.warn("OpenAPI fetch failed: {}", ex.getMessage());
        return respond(HttpStatus.BAD_GATEWAY, "OPENAPI_SOURCE_UNREACHABLE", ex.getMessage());
    }

    @ExceptionHandler(OpenApiParseException.class)
    public ResponseEntity<ErrorResponse> handleParseFailure(OpenApiParseException ex) {
        log.warn("OpenAPI parse failed: {}", ex.getMessage());
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, "OPENAPI_INVALID_CONTRACT", ex.getMessage());
    }

    @ExceptionHandler(SnapshotsNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSnapshotsNotAvailable(SnapshotsNotAvailableException ex) {
        log.info("Snapshots requested before a successful parse: {}", ex.getMessage());
        return respond(HttpStatus.CONFLICT, "CONTRACT_NOT_PARSED", ex.getMessage());
    }

    @ExceptionHandler(SnapshotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSnapshotNotFound(SnapshotNotFoundException ex) {
        log.info("Snapshot not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, "ENDPOINT_SNAPSHOT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.info("Request validation failed: {}", details);
        return respondWithDetails(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", details);
    }

    @ExceptionHandler(LlmNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleLlmNotConfigured(LlmNotConfiguredException ex) {
        log.warn("Scenario generation unavailable: {}", ex.getMessage());
        return respond(HttpStatus.SERVICE_UNAVAILABLE, "LLM_NOT_CONFIGURED", ex.getMessage());
    }

    @ExceptionHandler(LlmCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleLlmCommunication(LlmCommunicationException ex) {
        log.warn("LLM communication failure [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return respond(ex.getStatus(), ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(ScenarioArtifactNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleArtifactNotFound(ScenarioArtifactNotFoundException ex) {
        log.info("Scenario artifact not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, "SCENARIO_ARTIFACT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.info("Invalid parameter '{}': {}", ex.getName(), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Parameter '" + ex.getName() + "' has an invalid value");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadArgument(IllegalArgumentException ex) {
        log.info("Bad request argument: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String errorCode, String message) {
        return respondWithDetails(status, errorCode, message, List.of());
    }

    private ResponseEntity<ErrorResponse> respondWithDetails(HttpStatus status, String errorCode, String message,
            List<String> details) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), errorCode, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
