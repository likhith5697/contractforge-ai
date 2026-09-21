package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.FraudCase;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {
}
