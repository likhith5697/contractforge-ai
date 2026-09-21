package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.IdempotencyRecord;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    boolean existsByEndpointNameAndIdempotencyKey(String endpointName, String idempotencyKey);
}
