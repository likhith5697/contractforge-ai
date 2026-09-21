package com.likhith.bankingapi.entity.enums;

public final class CustomerEnums {

    private CustomerEnums() {
    }

    public enum Gender {
        MALE, FEMALE, NON_BINARY, PREFER_NOT_TO_SAY
    }

    public enum DocumentType {
        PASSPORT, NATIONAL_ID, DRIVER_LICENSE, VOTER_ID
    }

    public enum EmploymentType {
        SALARIED, SELF_EMPLOYED, BUSINESS_OWNER, RETIRED, STUDENT, UNEMPLOYED
    }

    public enum SourceOfFunds {
        SALARY, BUSINESS_INCOME, INVESTMENTS, INHERITANCE, SAVINGS, OTHER
    }

    public enum KycStatus {
        NOT_STARTED, PENDING, VERIFIED, REJECTED
    }

    public enum RiskRating {
        LOW, MEDIUM, HIGH
    }

    public enum OnboardingStatus {
        CREATED, PENDING_KYC, ACTIVE, SUSPENDED
    }
}
