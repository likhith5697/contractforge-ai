package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.FraudEnums.FraudCaseStatus;
import com.likhith.bankingapi.entity.enums.FraudEnums.FraudType;
import com.likhith.bankingapi.entity.enums.FraudEnums.Priority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Fraud case creation result")
public class FraudCaseResponse {

    @Schema(example = "FRD-1A2B3C4D5E6F")
    private String caseId;

    private String accountId;

    private FraudType fraudType;

    private Priority priority;

    private FraudCaseStatus status;

    private Instant createdAt;
}
