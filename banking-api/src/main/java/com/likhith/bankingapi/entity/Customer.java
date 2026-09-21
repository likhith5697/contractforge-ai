package com.likhith.bankingapi.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.CustomerEnums.EmploymentType;
import com.likhith.bankingapi.entity.enums.CustomerEnums.Gender;
import com.likhith.bankingapi.entity.enums.CustomerEnums.KycStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.OnboardingStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.RiskRating;
import com.likhith.bankingapi.entity.enums.CustomerEnums.SourceOfFunds;

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
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "alternate_phone_number")
    private String alternatePhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private com.likhith.bankingapi.entity.enums.CustomerEnums.DocumentType documentType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "document_issuing_country", nullable = false)
    private String documentIssuingCountry;

    @Column(name = "document_expiry_date", nullable = false)
    private LocalDate documentExpiryDate;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "employer_name")
    private String employerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "annual_income", precision = 19, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_of_funds")
    private SourceOfFunds sourceOfFunds;

    @Column(name = "expected_monthly_turnover", precision = 19, scale = 2)
    private BigDecimal expectedMonthlyTurnover;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_rating", nullable = false)
    @Builder.Default
    private RiskRating riskRating = RiskRating.LOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false)
    @Builder.Default
    private OnboardingStatus onboardingStatus = OnboardingStatus.CREATED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
