package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
