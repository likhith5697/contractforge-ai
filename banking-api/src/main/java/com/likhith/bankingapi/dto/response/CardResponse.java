package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CardEnums.CardNetwork;
import com.likhith.bankingapi.entity.enums.CardEnums.CardStatus;
import com.likhith.bankingapi.entity.enums.CardEnums.CardType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Card application result")
public class CardResponse {

    @Schema(example = "CARD-1A2B3C4D5E6F")
    private String cardId;

    private CardType cardType;

    private CardNetwork cardNetwork;

    @Schema(example = "**** **** **** 4821")
    private String maskedCardNumber;

    private CardStatus status;

    private Instant createdAt;
}
