package com.likhith.bankingapi.exception;

/**
 * Raised for any business-rule failure that Bean Validation cannot express,
 * e.g. "source and destination account must differ" or "account holder must exist".
 * Always mapped to HTTP 400.
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleViolationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
