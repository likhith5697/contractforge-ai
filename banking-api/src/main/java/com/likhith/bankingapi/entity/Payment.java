package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.PaymentEnums.BillerCategory;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentMethod;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentStatus;

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
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "biller_id", nullable = false)
    private String billerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "biller_category", nullable = false)
    private BillerCategory billerCategory;

    @Column(name = "consumer_number", nullable = false)
    private String consumerNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
