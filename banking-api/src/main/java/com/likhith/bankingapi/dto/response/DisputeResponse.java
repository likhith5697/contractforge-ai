package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeReason;
import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Dispute filing result")
public class DisputeResponse {

    @Schema(example = "DSP-1A2B3C4D5E6F")
    private String disputeId;

    private String transactionId;

    private DisputeReason disputeReason;

    private BigDecimal disputedAmount;

    private String currency;

    private DisputeStatus status;

    private Instant createdAt;
}
