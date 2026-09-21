package com.likhith.bankingapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByLoanId(String loanId);
}
