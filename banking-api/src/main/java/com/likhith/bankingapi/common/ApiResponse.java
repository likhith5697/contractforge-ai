package com.likhith.bankingapi.common;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Consistent success envelope returned by every endpoint in this API.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard success response envelope")
public class ApiResponse<T> {

    @Schema(description = "Correlation id for this request, echoed from X-Request-Id or generated", example = "3f2b1a4c-7e21-4b9a-9c3d-1a2b3c4d5e6f")
    private final String requestId;

    @Schema(description = "Outcome status", example = "SUCCESS")
    private final String status;

    @Schema(description = "Human readable message describing the result", example = "Customer onboarded successfully")
    private final String message;

    @Schema(description = "Response payload")
    private final T data;

    @Schema(description = "Server timestamp when the response was generated")
    private final Instant timestamp;

    private ApiResponse(String requestId, String status, String message, T data) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(RequestIdHolder.get(), "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(RequestIdHolder.get(), "CREATED", message, data);
    }
}
