package com.likhith.bankingapi.entity.enums;

public final class MerchantPaymentEnums {

    private MerchantPaymentEnums() {
    }

    public enum MerchantCategory {
        RETAIL, FOOD_AND_DINING, TRAVEL, UTILITIES, ENTERTAINMENT, GROCERY, ECOMMERCE
    }

    public enum MerchantPaymentStatus {
        AUTHORIZED, CAPTURED, FAILED, REFUNDED
    }

    public enum PaymentInstrument {
        ACCOUNT_BALANCE, DEBIT_CARD, CREDIT_CARD, WALLET
    }
}
