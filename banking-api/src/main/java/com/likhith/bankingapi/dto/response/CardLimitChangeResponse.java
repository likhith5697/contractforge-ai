package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CardEnums.LimitType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Card limit change result")
public class CardLimitChangeResponse {

    @Schema(example = "CLC-1A2B3C4D5E6F")
    private String changeId;

    @Schema(example = "CARD-1A2B3C4D5E6F")
    private String cardId;

    private LimitType limitType;

    private BigDecimal approvedLimit;

    private String currency;

    private Instant effectiveFrom;
}
