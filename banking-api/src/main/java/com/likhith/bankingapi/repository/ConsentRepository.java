package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Consent;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
}
