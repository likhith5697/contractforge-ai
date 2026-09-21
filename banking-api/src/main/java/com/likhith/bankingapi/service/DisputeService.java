package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.CreateDisputeRequest;
import com.likhith.bankingapi.dto.response.DisputeResponse;
import com.likhith.bankingapi.entity.Dispute;
import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.DisputeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public DisputeResponse fileDispute(CreateDisputeRequest request, String idempotencyKey) {
        idempotencyService.reserve("disputes", idempotencyKey);

        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        Dispute dispute = Dispute.builder()
                .disputeId(IdGenerator.generate("DSP"))
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .disputeReason(request.getDisputeReason())
                .disputedAmount(request.getDisputedAmount().getAmount())
                .currency(request.getDisputedAmount().getCurrency())
                .description(request.getDescription())
                .status(DisputeStatus.FILED)
                .createdAt(Instant.now())
                .build();

        dispute = disputeRepository.save(dispute);
        log.info("Filed dispute {} for transaction {}", dispute.getDisputeId(), request.getTransactionId());

        return DisputeResponse.builder()
                .disputeId(dispute.getDisputeId())
                .transactionId(dispute.getTransactionId())
                .disputeReason(dispute.getDisputeReason())
                .disputedAmount(dispute.getDisputedAmount())
                .currency(dispute.getCurrency())
                .status(dispute.getStatus())
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}
