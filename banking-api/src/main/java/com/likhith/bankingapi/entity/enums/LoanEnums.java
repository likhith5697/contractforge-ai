package com.likhith.bankingapi.entity.enums;

public final class LoanEnums {

    private LoanEnums() {
    }

    public enum LoanType {
        PERSONAL, HOME, AUTO, EDUCATION
    }

    public enum LoanStatus {
        SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED
    }

    public enum CollateralType {
        NONE, PROPERTY, VEHICLE, FIXED_DEPOSIT
    }

    public enum LoanDocumentType {
        INCOME_PROOF, ID_PROOF, ADDRESS_PROOF, BANK_STATEMENT, PROPERTY_PAPERS
    }

    public enum UploadedByType {
        CUSTOMER, BRANCH_STAFF, AGENT
    }
}
