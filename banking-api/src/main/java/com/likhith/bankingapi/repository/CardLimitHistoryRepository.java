package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.CardLimitHistory;

public interface CardLimitHistoryRepository extends JpaRepository<CardLimitHistory, Long> {
}
