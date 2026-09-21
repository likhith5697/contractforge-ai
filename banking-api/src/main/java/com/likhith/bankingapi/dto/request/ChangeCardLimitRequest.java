package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;

import com.likhith.bankingapi.entity.enums.CardEnums.LimitType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to change a card's spending limit")
public class ChangeCardLimitRequest {

    @NotNull(message = "limitType is required")
    private LimitType limitType;

    @NotNull(message = "requestedLimit is required")
    @DecimalMin(value = "0.01", message = "requestedLimit must be greater than zero")
    @Schema(example = "50000.00")
    private BigDecimal requestedLimit;

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code")
    @Schema(example = "USD")
    private String currency;

    @NotBlank(message = "reason is required")
    @Size(max = 200)
    @Schema(example = "Upcoming international travel")
    private String reason;

    @NotNull(message = "effectiveImmediately is required")
    private Boolean effectiveImmediately;

    @NotNull(message = "approvalMetadata is required")
    @Valid
    private ApprovalMetadata approvalMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Approval trail for the limit change")
    public static class ApprovalMetadata {

        @Schema(example = "AGENT-4471")
        private String approvedBy;

        @NotNull(message = "otpVerified is required")
        private Boolean otpVerified;
    }
}
