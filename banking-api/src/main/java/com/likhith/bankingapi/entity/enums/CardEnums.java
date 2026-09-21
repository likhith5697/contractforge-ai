package com.likhith.bankingapi.entity.enums;

public final class CardEnums {

    private CardEnums() {
    }

    public enum CardType {
        DEBIT, CREDIT, PREPAID
    }

    public enum CardNetwork {
        VISA, MASTERCARD, RUPAY, AMEX
    }

    public enum CardStatus {
        APPLIED, ACTIVE, BLOCKED, EXPIRED
    }

    public enum LimitType {
        DAILY_ATM, DAILY_POS, DAILY_ONLINE, MONTHLY_TOTAL
    }
}
