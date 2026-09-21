package com.likhith.bankingapi.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.config.CardLimitsProperties;
import com.likhith.bankingapi.dto.request.ActivateCardRequest;
import com.likhith.bankingapi.dto.request.CardApplicationRequest;
import com.likhith.bankingapi.dto.request.ChangeCardLimitRequest;
import com.likhith.bankingapi.dto.response.CardActivationResponse;
import com.likhith.bankingapi.dto.response.CardLimitChangeResponse;
import com.likhith.bankingapi.dto.response.CardResponse;
import com.likhith.bankingapi.entity.Card;
import com.likhith.bankingapi.entity.CardLimitHistory;
import com.likhith.bankingapi.entity.enums.CardEnums.CardStatus;
import com.likhith.bankingapi.entity.enums.CardEnums.LimitType;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.exception.ResourceNotFoundException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.CardLimitHistoryRepository;
import com.likhith.bankingapi.repository.CardRepository;
import com.likhith.bankingapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CardRepository cardRepository;
    private final CardLimitHistoryRepository cardLimitHistoryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardLimitsProperties cardLimitsProperties;
    private final IdempotencyService idempotencyService;

    @Transactional
    public CardResponse applyForCard(CardApplicationRequest request, String idempotencyKey) {
        idempotencyService.reserve("cards.application", idempotencyKey);

        if (!customerRepository.existsByCustomerId(request.getCustomerId())) {
            throw new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                    "Customer '" + request.getCustomerId() + "' does not exist");
        }
        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        String maskedNumber = "**** **** **** " + (1000 + RANDOM.nextInt(9000));

        Card card = Card.builder()
                .cardId(IdGenerator.generate("CARD"))
                .customerId(request.getCustomerId())
                .accountId(request.getAccountId())
                .cardType(request.getCardType())
                .cardNetwork(request.getCardNetwork())
                .cardholderName(request.getCardholderName())
                .maskedCardNumber(maskedNumber)
                .virtualCard(Boolean.TRUE.equals(request.getIsVirtualCard()))
                .creditLimit(request.getRequestedCreditLimit())
                .dailyAtmLimit(cardLimitsProperties.getDailyAtmMax())
                .dailyPosLimit(cardLimitsProperties.getDailyPosMax())
                .dailyOnlineLimit(cardLimitsProperties.getDailyOnlineMax())
                .monthlyTotalLimit(cardLimitsProperties.getMonthlyTotalMax())
                .status(CardStatus.APPLIED)
                .createdAt(Instant.now())
                .build();

        card = cardRepository.save(card);
        log.info("Card application {} created for customer {}", card.getCardId(), request.getCustomerId());

        return CardResponse.builder()
                .cardId(card.getCardId())
                .cardType(card.getCardType())
                .cardNetwork(card.getCardNetwork())
                .maskedCardNumber(card.getMaskedCardNumber())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .build();
    }

    @Transactional
    public CardActivationResponse activateCard(String cardId, ActivateCardRequest request) {
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("CARD_NOT_FOUND",
                        "Card '" + cardId + "' does not exist"));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new BusinessRuleViolationException("CARD_ALREADY_ACTIVE",
                    "Card '" + cardId + "' has already been activated");
        }
        if (!card.getMaskedCardNumber().endsWith(request.getLastFourDigits())) {
            throw new BusinessRuleViolationException("CARD_DETAILS_MISMATCH",
                    "lastFourDigits does not match the card on file");
        }

        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        log.info("Card {} activated", cardId);

        return CardActivationResponse.builder()
                .cardId(card.getCardId())
                .status(card.getStatus())
                .activatedAt(Instant.now())
                .build();
    }

    @Transactional
    public CardLimitChangeResponse changeCardLimit(String cardId, ChangeCardLimitRequest request) {
        Card card = cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("CARD_NOT_FOUND",
                        "Card '" + cardId + "' does not exist"));

        BigDecimal maxAllowed = maxAllowedFor(request.getLimitType());
        if (request.getRequestedLimit().compareTo(maxAllowed) > 0) {
            throw new BusinessRuleViolationException("LIMIT_EXCEEDS_MAXIMUM",
                    "requestedLimit for " + request.getLimitType() + " must not exceed " + maxAllowed);
        }

        applyLimit(card, request.getLimitType(), request.getRequestedLimit());
        cardRepository.save(card);

        CardLimitHistory history = CardLimitHistory.builder()
                .changeId(IdGenerator.generate("CLC"))
                .cardId(cardId)
                .limitType(request.getLimitType())
                .requestedLimit(request.getRequestedLimit())
                .currency(request.getCurrency())
                .reason(request.getReason())
                .effectiveImmediately(Boolean.TRUE.equals(request.getEffectiveImmediately()))
                .createdAt(Instant.now())
                .build();
        history = cardLimitHistoryRepository.save(history);
        log.info("Card {} limit {} changed to {}", cardId, request.getLimitType(), request.getRequestedLimit());

        return CardLimitChangeResponse.builder()
                .changeId(history.getChangeId())
                .cardId(cardId)
                .limitType(history.getLimitType())
                .approvedLimit(history.getRequestedLimit())
                .currency(history.getCurrency())
                .effectiveFrom(history.getCreatedAt())
                .build();
    }

    private BigDecimal maxAllowedFor(LimitType limitType) {
        return switch (limitType) {
            case DAILY_ATM -> cardLimitsProperties.getDailyAtmMax();
            case DAILY_POS -> cardLimitsProperties.getDailyPosMax();
            case DAILY_ONLINE -> cardLimitsProperties.getDailyOnlineMax();
            case MONTHLY_TOTAL -> cardLimitsProperties.getMonthlyTotalMax();
        };
    }

    private void applyLimit(Card card, LimitType limitType, BigDecimal newLimit) {
        switch (limitType) {
            case DAILY_ATM -> card.setDailyAtmLimit(newLimit);
            case DAILY_POS -> card.setDailyPosLimit(newLimit);
            case DAILY_ONLINE -> card.setDailyOnlineLimit(newLimit);
            case MONTHLY_TOTAL -> card.setMonthlyTotalLimit(newLimit);
        }
    }
}
