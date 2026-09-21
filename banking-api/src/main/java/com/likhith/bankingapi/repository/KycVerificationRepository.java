package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.KycVerification;

public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {
}
