package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.TransferEnums.TransferStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "International wire transfer result")
public class InternationalTransferResponse {

    @Schema(example = "ITX-A1B2C3D4E5F6")
    private String transferId;

    private String sourceAccountId;

    private String beneficiaryName;

    private String swiftBic;

    private BigDecimal amount;

    private String currency;

    private TransferStatus status;

    private Instant createdAt;
}
