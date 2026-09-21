package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.LoanEnums.LoanStatus;
import com.likhith.bankingapi.entity.enums.LoanEnums.LoanType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Loan application result")
public class LoanApplicationResponse {

    @Schema(example = "LOAN-1A2B3C4D5E6F")
    private String loanId;

    private String customerId;

    private LoanType loanType;

    private BigDecimal requestedAmount;

    private String currency;

    private LoanStatus status;

    private Instant createdAt;
}
