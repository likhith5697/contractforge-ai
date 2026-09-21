package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CardEnums.LimitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_limit_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardLimitHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_id", nullable = false, unique = true)
    private String changeId;

    @Column(name = "card_id", nullable = false)
    private String cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false)
    private LimitType limitType;

    @Column(name = "requested_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedLimit;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "effective_immediately", nullable = false)
    private boolean effectiveImmediately;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
