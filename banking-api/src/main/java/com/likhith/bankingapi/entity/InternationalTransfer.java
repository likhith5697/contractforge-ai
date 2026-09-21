package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.TransferEnums.ChargeOption;
import com.likhith.bankingapi.entity.enums.TransferEnums.InternationalPurpose;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferStatus;

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
@Table(name = "international_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternationalTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, unique = true)
    private String transferId;

    @Column(name = "source_account_id", nullable = false)
    private String sourceAccountId;

    @Column(name = "beneficiary_name", nullable = false)
    private String beneficiaryName;

    @Column(name = "beneficiary_country", nullable = false)
    private String beneficiaryCountry;

    @Column(name = "swift_bic", nullable = false)
    private String swiftBic;

    @Column(name = "iban", nullable = false)
    private String iban;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_of_transfer", nullable = false)
    private InternationalPurpose purposeOfTransfer;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_option", nullable = false)
    private ChargeOption chargeOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
