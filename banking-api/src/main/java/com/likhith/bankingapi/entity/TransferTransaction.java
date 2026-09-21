package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.TransferEnums.PurposeCode;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferStatus;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferType;

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
@Table(name = "transfer_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, unique = true)
    private String transferId;

    @Column(name = "source_account_id", nullable = false)
    private String sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private String destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_code")
    private PurposeCode purposeCode;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
