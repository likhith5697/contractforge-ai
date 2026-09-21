package com.likhith.bankingapi.entity.enums;

public final class PaymentEnums {

    private PaymentEnums() {
    }

    public enum BillerCategory {
        ELECTRICITY, WATER, INTERNET, MOBILE_POSTPAID, INSURANCE, CREDIT_CARD
    }

    public enum PaymentMethod {
        ACCOUNT_BALANCE, LINKED_CARD
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, SCHEDULED, CANCELLED
    }

    public enum PaymentFrequency {
        ONE_TIME, WEEKLY, MONTHLY, QUARTERLY
    }
}
