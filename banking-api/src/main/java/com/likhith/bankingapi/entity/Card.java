package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CardEnums.CardNetwork;
import com.likhith.bankingapi.entity.enums.CardEnums.CardStatus;
import com.likhith.bankingapi.entity.enums.CardEnums.CardType;

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
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_network", nullable = false)
    private CardNetwork cardNetwork;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "masked_card_number", nullable = false)
    private String maskedCardNumber;

    @Column(name = "is_virtual_card", nullable = false)
    private boolean virtualCard;

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "daily_atm_limit", precision = 19, scale = 2)
    private BigDecimal dailyAtmLimit;

    @Column(name = "daily_pos_limit", precision = 19, scale = 2)
    private BigDecimal dailyPosLimit;

    @Column(name = "daily_online_limit", precision = 19, scale = 2)
    private BigDecimal dailyOnlineLimit;

    @Column(name = "monthly_total_limit", precision = 19, scale = 2)
    private BigDecimal monthlyTotalLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.APPLIED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
