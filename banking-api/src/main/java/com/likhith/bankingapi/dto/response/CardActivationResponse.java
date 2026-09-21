package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CardEnums.CardStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Card activation result")
public class CardActivationResponse {

    @Schema(example = "CARD-1A2B3C4D5E6F")
    private String cardId;

    private CardStatus status;

    private Instant activatedAt;
}
