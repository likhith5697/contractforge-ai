package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.CreateFraudCaseRequest;
import com.likhith.bankingapi.dto.response.FraudCaseResponse;
import com.likhith.bankingapi.entity.FraudCase;
import com.likhith.bankingapi.entity.enums.FraudEnums.FraudCaseStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.FraudCaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudCaseService {

    private static final Logger log = LoggerFactory.getLogger(FraudCaseService.class);

    private final FraudCaseRepository fraudCaseRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public FraudCaseResponse createCase(CreateFraudCaseRequest request) {
        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        FraudCase fraudCase = FraudCase.builder()
                .caseId(IdGenerator.generate("FRD"))
                .reportedBy(request.getReportedBy())
                .relatedTransactionId(request.getRelatedTransactionId())
                .accountId(request.getAccountId())
                .fraudType(request.getFraudType())
                .description(request.getDescription())
                .estimatedLossAmount(request.getEstimatedLoss() != null ? request.getEstimatedLoss().getAmount() : null)
                .estimatedLossCurrency(request.getEstimatedLoss() != null ? request.getEstimatedLoss().getCurrency() : null)
                .priority(request.getPriority())
                .status(FraudCaseStatus.OPEN)
                .createdAt(Instant.now())
                .build();

        fraudCase = fraudCaseRepository.save(fraudCase);
        log.info("Opened fraud case {} for account {}", fraudCase.getCaseId(), request.getAccountId());

        return FraudCaseResponse.builder()
                .caseId(fraudCase.getCaseId())
                .accountId(fraudCase.getAccountId())
                .fraudType(fraudCase.getFraudType())
                .priority(fraudCase.getPriority())
                .status(fraudCase.getStatus())
                .createdAt(fraudCase.getCreatedAt())
                .build();
    }
}
