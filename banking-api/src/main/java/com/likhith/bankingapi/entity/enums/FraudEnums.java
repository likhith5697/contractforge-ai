package com.likhith.bankingapi.entity.enums;

public final class FraudEnums {

    private FraudEnums() {
    }

    public enum ReportedBy {
        CUSTOMER, SYSTEM, BANK_STAFF
    }

    public enum FraudType {
        UNAUTHORIZED_TRANSACTION, PHISHING, CARD_SKIMMING, IDENTITY_THEFT, ACCOUNT_TAKEOVER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum FraudCaseStatus {
        OPEN, INVESTIGATING, RESOLVED, CLOSED
    }

    public enum EvidenceType {
        SCREENSHOT, EMAIL, SMS, CALL_RECORDING, TRANSACTION_LOG, OTHER
    }
}
