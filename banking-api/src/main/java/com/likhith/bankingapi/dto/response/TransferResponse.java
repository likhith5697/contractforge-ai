package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.TransferEnums.TransferStatus;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Domestic transfer result")
public class TransferResponse {

    @Schema(example = "TRF-1234567890AB")
    private String transferId;

    private String sourceAccountId;

    private String destinationAccountId;

    private BigDecimal amount;

    private String currency;

    private TransferType transferType;

    private TransferStatus status;

    private Instant createdAt;
}
