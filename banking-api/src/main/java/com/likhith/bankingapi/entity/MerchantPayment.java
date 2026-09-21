package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.MerchantCategory;
import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.MerchantPaymentStatus;
import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.PaymentInstrument;

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
@Table(name = "merchant_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_payment_id", nullable = false, unique = true)
    private String merchantPaymentId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "merchant_category", nullable = false)
    private MerchantCategory merchantCategory;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_instrument", nullable = false)
    private PaymentInstrument paymentInstrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MerchantPaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
