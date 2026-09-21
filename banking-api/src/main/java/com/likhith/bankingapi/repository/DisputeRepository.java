package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Dispute;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
}
