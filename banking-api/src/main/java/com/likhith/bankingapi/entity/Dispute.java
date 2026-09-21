package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeReason;
import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeStatus;

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
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispute_id", nullable = false, unique = true)
    private String disputeId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_reason", nullable = false)
    private DisputeReason disputeReason;

    @Column(name = "disputed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal disputedAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.FILED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
