package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.KycVerificationRequest;
import com.likhith.bankingapi.dto.response.KycVerificationResponse;
import com.likhith.bankingapi.entity.KycVerification;
import com.likhith.bankingapi.entity.enums.KycEnums.KycVerificationStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.CustomerRepository;
import com.likhith.bankingapi.repository.KycVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    private final KycVerificationRepository kycVerificationRepository;
    private final CustomerRepository customerRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public KycVerificationResponse verify(KycVerificationRequest request, String idempotencyKey) {
        idempotencyService.reserve("kyc.verifications", idempotencyKey);

        if (!customerRepository.existsByCustomerId(request.getCustomerId())) {
            throw new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                    "Customer '" + request.getCustomerId() + "' does not exist");
        }

        KycVerification verification = KycVerification.builder()
                .verificationId(IdGenerator.generate("KYC"))
                .customerId(request.getCustomerId())
                .verificationType(request.getVerificationType())
                .verificationChannel(request.getVerificationChannel())
                .documentNumber(request.getDocumentDetails().getDocumentNumber())
                .status(KycVerificationStatus.VERIFIED)
                .createdAt(Instant.now())
                .build();

        verification = kycVerificationRepository.save(verification);
        log.info("Recorded KYC verification {} for customer {}", verification.getVerificationId(),
                request.getCustomerId());

        return KycVerificationResponse.builder()
                .verificationId(verification.getVerificationId())
                .customerId(verification.getCustomerId())
                .verificationType(verification.getVerificationType())
                .status(verification.getStatus())
                .createdAt(verification.getCreatedAt())
                .build();
    }
}
