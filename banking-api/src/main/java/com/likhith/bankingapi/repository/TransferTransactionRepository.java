package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.TransferTransaction;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
}
