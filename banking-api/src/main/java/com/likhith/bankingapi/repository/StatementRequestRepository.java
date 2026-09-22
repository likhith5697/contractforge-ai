package com.likhith.bankingapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.StatementRequest;

public interface StatementRequestRepository extends JpaRepository<StatementRequest, Long> {

    Optional<StatementRequest> findByStatementRequestId(String statementRequestId);
}
