package com.likhith.bankingapi.entity.enums;

public final class StatementEnums {

    private StatementEnums() {
    }

    public enum StatementFormat {
        PDF, CSV, XML
    }

    public enum StatementPeriodType {
        LAST_MONTH, LAST_QUARTER, LAST_SIX_MONTHS, CUSTOM_RANGE
    }

    public enum StatementDeliveryMethod {
        EMAIL, DOWNLOAD_LINK, POSTAL_MAIL
    }

    public enum StatementRequestStatus {
        RECEIVED, PROCESSING, READY, FAILED
    }
}
