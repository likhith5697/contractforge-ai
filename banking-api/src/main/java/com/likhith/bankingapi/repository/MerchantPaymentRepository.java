package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.MerchantPayment;

public interface MerchantPaymentRepository extends JpaRepository<MerchantPayment, Long> {
}
