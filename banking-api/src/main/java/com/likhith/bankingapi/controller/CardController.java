package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.ActivateCardRequest;
import com.likhith.bankingapi.dto.request.CardApplicationRequest;
import com.likhith.bankingapi.dto.request.ChangeCardLimitRequest;
import com.likhith.bankingapi.dto.response.CardActivationResponse;
import com.likhith.bankingapi.dto.response.CardLimitChangeResponse;
import com.likhith.bankingapi.dto.response.CardResponse;
import com.likhith.bankingapi.service.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Card application, activation and limit management")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Apply for a new debit, credit or prepaid card")
    public ResponseEntity<ApiResponse<CardResponse>> applyForCard(
            @Valid @RequestBody CardApplicationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received card application for customer {}", request.getCustomerId());
        CardResponse response = cardService.applyForCard(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Card application submitted successfully", response));
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Activate an issued card")
    public ResponseEntity<ApiResponse<CardActivationResponse>> activateCard(
            @PathVariable String cardId,
            @Valid @RequestBody ActivateCardRequest request) {
        log.info("Received activation request for card {}", cardId);
        CardActivationResponse response = cardService.activateCard(cardId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Card activated successfully", response));
    }

    @PostMapping("/{cardId}/limits")
    @Operation(summary = "Change a card's spending limit")
    public ResponseEntity<ApiResponse<CardLimitChangeResponse>> changeCardLimit(
            @PathVariable String cardId,
            @Valid @RequestBody ChangeCardLimitRequest request) {
        log.info("Received limit change request for card {}", cardId);
        CardLimitChangeResponse response = cardService.changeCardLimit(cardId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Card limit updated successfully", response));
    }
}
