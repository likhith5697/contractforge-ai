package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Beneficiary registration result")
public class BeneficiaryResponse {

    @Schema(example = "BEN-4C5D6E7F8A9B")
    private String beneficiaryId;

    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    private String beneficiaryName;

    private String bankName;

    private BigDecimal dailyTransferLimit;

    private boolean trusted;

    private Instant createdAt;
}
