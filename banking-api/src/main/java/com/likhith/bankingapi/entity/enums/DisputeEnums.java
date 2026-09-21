package com.likhith.bankingapi.entity.enums;

public final class DisputeEnums {

    private DisputeEnums() {
    }

    public enum DisputeReason {
        UNAUTHORIZED, DUPLICATE_CHARGE, GOODS_NOT_RECEIVED, INCORRECT_AMOUNT, SERVICE_NOT_AS_DESCRIBED
    }

    public enum DisputeStatus {
        FILED, UNDER_REVIEW, RESOLVED, REJECTED
    }
}
