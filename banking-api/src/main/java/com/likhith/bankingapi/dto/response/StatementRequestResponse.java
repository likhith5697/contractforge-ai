package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.StatementEnums.StatementFormat;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementRequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Statement request acknowledgement")
public class StatementRequestResponse {

    @Schema(example = "STR-1A2B3C4D5E6F")
    private String statementRequestId;

    private String accountId;

    private StatementFormat format;

    private StatementRequestStatus status;

    private Instant createdAt;
}
