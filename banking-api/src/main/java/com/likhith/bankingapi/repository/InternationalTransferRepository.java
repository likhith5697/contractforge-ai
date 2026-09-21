package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.InternationalTransfer;

public interface InternationalTransferRepository extends JpaRepository<InternationalTransfer, Long> {
}
