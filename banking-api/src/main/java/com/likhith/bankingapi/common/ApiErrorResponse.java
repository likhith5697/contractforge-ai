package com.likhith.bankingapi.common;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Consistent error envelope returned by every failure path in this API.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response envelope")
public class ApiErrorResponse {

    @Schema(description = "Correlation id for this request, echoed from X-Request-Id or generated")
    private final String requestId;

    @Schema(description = "HTTP status text", example = "BAD_REQUEST")
    private final String status;

    @Schema(description = "Machine readable error code", example = "VALIDATION_FAILED")
    private final String errorCode;

    @Schema(description = "Human readable error message", example = "Request validation failed")
    private final String message;

    @Schema(description = "Field level validation errors, present only for validation failures")
    private final List<FieldErrorDetail> fieldErrors;

    @Schema(description = "Server timestamp when the error was generated")
    private final Instant timestamp;

    public ApiErrorResponse(String status, String errorCode, String message, List<FieldErrorDetail> fieldErrors) {
        this.requestId = RequestIdHolder.get();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }
}
