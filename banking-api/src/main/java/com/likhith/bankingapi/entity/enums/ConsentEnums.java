package com.likhith.bankingapi.entity.enums;

public final class ConsentEnums {

    private ConsentEnums() {
    }

    public enum ConsentType {
        DATA_SHARING, MARKETING_COMMUNICATION, CREDIT_BUREAU_CHECK, OPEN_BANKING_ACCESS, TERMS_AND_CONDITIONS
    }

    public enum ConsentStatus {
        GRANTED, DENIED, REVOKED
    }
}
