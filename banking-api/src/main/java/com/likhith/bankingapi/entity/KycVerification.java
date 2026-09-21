package com.likhith.bankingapi.entity;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.KycEnums.KycVerificationStatus;
import com.likhith.bankingapi.entity.enums.KycEnums.VerificationChannel;
import com.likhith.bankingapi.entity.enums.KycEnums.VerificationType;

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
@Table(name = "kyc_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", nullable = false, unique = true)
    private String verificationId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_channel", nullable = false)
    private VerificationChannel verificationChannel;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private KycVerificationStatus status = KycVerificationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
