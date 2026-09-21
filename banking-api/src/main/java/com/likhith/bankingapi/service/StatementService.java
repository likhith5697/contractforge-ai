package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.CreateStatementRequest;
import com.likhith.bankingapi.dto.response.StatementRequestResponse;
import com.likhith.bankingapi.entity.StatementRequest;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementDeliveryMethod;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementPeriodType;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementRequestStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.StatementRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementService {

    private static final Logger log = LoggerFactory.getLogger(StatementService.class);

    private final StatementRequestRepository statementRequestRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public StatementRequestResponse requestStatement(CreateStatementRequest request) {
        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        if (request.getPeriodType() == StatementPeriodType.CUSTOM_RANGE) {
            if (request.getCustomRange() == null || request.getCustomRange().getStartDate() == null
                    || request.getCustomRange().getEndDate() == null) {
                throw new BusinessRuleViolationException("CUSTOM_RANGE_REQUIRED",
                        "customRange with startDate and endDate is required when periodType is CUSTOM_RANGE");
            }
            if (!request.getCustomRange().getStartDate().isBefore(request.getCustomRange().getEndDate())) {
                throw new BusinessRuleViolationException("INVALID_CUSTOM_RANGE",
                        "customRange.startDate must be before customRange.endDate");
            }
        }

        if (request.getDeliveryMethod() == StatementDeliveryMethod.EMAIL
                && (request.getDeliveryEmail() == null || request.getDeliveryEmail().isBlank())) {
            throw new BusinessRuleViolationException("DELIVERY_EMAIL_REQUIRED",
                    "deliveryEmail is required when deliveryMethod is EMAIL");
        }

        StatementRequest statementRequest = StatementRequest.builder()
                .statementRequestId(IdGenerator.generate("STR"))
                .accountId(request.getAccountId())
                .periodType(request.getPeriodType())
                .customStartDate(request.getCustomRange() != null ? request.getCustomRange().getStartDate() : null)
                .customEndDate(request.getCustomRange() != null ? request.getCustomRange().getEndDate() : null)
                .format(request.getFormat())
                .deliveryMethod(request.getDeliveryMethod())
                .deliveryEmail(request.getDeliveryEmail())
                .status(StatementRequestStatus.RECEIVED)
                .createdAt(Instant.now())
                .build();

        statementRequest = statementRequestRepository.save(statementRequest);
        log.info("Statement request {} received for account {}", statementRequest.getStatementRequestId(),
                request.getAccountId());

        return StatementRequestResponse.builder()
                .statementRequestId(statementRequest.getStatementRequestId())
                .accountId(statementRequest.getAccountId())
                .format(statementRequest.getFormat())
                .status(statementRequest.getStatus())
                .createdAt(statementRequest.getCreatedAt())
                .build();
    }
}
