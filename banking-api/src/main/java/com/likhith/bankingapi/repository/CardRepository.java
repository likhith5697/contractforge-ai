package com.likhith.bankingapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardId(String cardId);
}
