package com.likhith.bankingapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);
}
