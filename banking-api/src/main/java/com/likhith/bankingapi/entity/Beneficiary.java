package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CommonEnums.RelationshipType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "beneficiary_id", nullable = false, unique = true)
    private String beneficiaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "beneficiary_name", nullable = false)
    private String beneficiaryName;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship")
    private RelationshipType relationship;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_account_number", nullable = false)
    private String bankAccountNumber;

    @Column(name = "routing_code", nullable = false)
    private String routingCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "daily_transfer_limit", precision = 19, scale = 2)
    private BigDecimal dailyTransferLimit;

    @Column(name = "is_trusted", nullable = false)
    private boolean trusted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
