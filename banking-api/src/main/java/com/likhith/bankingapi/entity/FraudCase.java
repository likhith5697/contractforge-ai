package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.FraudEnums.FraudCaseStatus;
import com.likhith.bankingapi.entity.enums.FraudEnums.FraudType;
import com.likhith.bankingapi.entity.enums.FraudEnums.Priority;
import com.likhith.bankingapi.entity.enums.FraudEnums.ReportedBy;

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
@Table(name = "fraud_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_by", nullable = false)
    private ReportedBy reportedBy;

    @Column(name = "related_transaction_id")
    private String relatedTransactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_type", nullable = false)
    private FraudType fraudType;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "estimated_loss_amount", precision = 19, scale = 2)
    private BigDecimal estimatedLossAmount;

    @Column(name = "estimated_loss_currency", length = 3)
    private String estimatedLossCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FraudCaseStatus status = FraudCaseStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
