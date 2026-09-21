package com.likhith.bankingapi.entity;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentStatus;
import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentType;

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
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id", nullable = false, unique = true)
    private String consentId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false)
    private ConsentType consentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;

    @Column(name = "consent_version", nullable = false)
    private String consentVersion;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
