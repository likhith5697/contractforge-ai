package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.entity.IdempotencyRecord;
import com.likhith.bankingapi.exception.DuplicateIdempotencyKeyException;
import com.likhith.bankingapi.repository.IdempotencyRecordRepository;

import lombok.RequiredArgsConstructor;

/**
 * Guards transactional creation endpoints against replayed requests carrying the
 * same Idempotency-Key header. Reservation happens before the business operation
 * runs, so a repeated key is rejected with 409 before any side effect occurs.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    @Transactional
    public void reserve(String endpointName, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        if (idempotencyRecordRepository.existsByEndpointNameAndIdempotencyKey(endpointName, idempotencyKey)) {
            log.warn("Duplicate idempotency key '{}' rejected for endpoint '{}'", idempotencyKey, endpointName);
            throw new DuplicateIdempotencyKeyException(idempotencyKey);
        }
        idempotencyRecordRepository.save(IdempotencyRecord.builder()
                .endpointName(endpointName)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build());
    }
}
