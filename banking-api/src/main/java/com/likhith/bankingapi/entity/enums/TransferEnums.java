package com.likhith.bankingapi.entity.enums;

public final class TransferEnums {

    private TransferEnums() {
    }

    public enum TransferType {
        IMPS, NEFT, RTGS, INSTANT
    }

    public enum TransferStatus {
        PENDING, COMPLETED, FAILED, SCHEDULED
    }

    public enum PurposeCode {
        SALARY, GOODS_SERVICES, LOAN_REPAYMENT, FAMILY_SUPPORT, OTHER
    }

    public enum InternationalPurpose {
        TRADE, EDUCATION, FAMILY_SUPPORT, INVESTMENT, MEDICAL, OTHER
    }

    public enum ChargeOption {
        OUR, SHA, BEN
    }
}
