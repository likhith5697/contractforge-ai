package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.LoanEnums.LoanStatus;
import com.likhith.bankingapi.entity.enums.LoanEnums.LoanType;

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
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false, unique = true)
    private String loanId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.SUBMITTED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
