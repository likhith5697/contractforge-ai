package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.AccountEnums.AccountStatus;
import com.likhith.bankingapi.entity.enums.AccountEnums.AccountType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Newly opened account details")
public class AccountResponse {

    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    private AccountType accountType;

    private String currency;

    private BigDecimal balance;

    private AccountStatus status;

    private Instant createdAt;
}
