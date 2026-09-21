package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.CreateConsentRequest;
import com.likhith.bankingapi.dto.response.ConsentResponse;
import com.likhith.bankingapi.entity.Consent;
import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.ConsentRepository;
import com.likhith.bankingapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

    private final ConsentRepository consentRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public ConsentResponse recordConsent(CreateConsentRequest request) {
        if (!customerRepository.existsByCustomerId(request.getCustomerId())) {
            throw new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                    "Customer '" + request.getCustomerId() + "' does not exist");
        }

        Consent consent = Consent.builder()
                .consentId(IdGenerator.generate("CNS"))
                .customerId(request.getCustomerId())
                .consentType(request.getConsentType())
                .status(Boolean.TRUE.equals(request.getGranted()) ? ConsentStatus.GRANTED : ConsentStatus.DENIED)
                .consentVersion(request.getConsentVersion())
                .expiresAt(request.getExpiresAt())
                .createdAt(Instant.now())
                .build();

        consent = consentRepository.save(consent);
        log.info("Recorded consent {} ({}) for customer {}", consent.getConsentId(), consent.getStatus(),
                request.getCustomerId());

        return ConsentResponse.builder()
                .consentId(consent.getConsentId())
                .customerId(consent.getCustomerId())
                .consentType(consent.getConsentType())
                .status(consent.getStatus())
                .expiresAt(consent.getExpiresAt())
                .createdAt(consent.getCreatedAt())
                .build();
    }
}
