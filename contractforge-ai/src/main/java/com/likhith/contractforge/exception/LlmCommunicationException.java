package com.likhith.contractforge.exception;

import org.springframework.http.HttpStatus;

/**
 * A call to OpenAI failed - auth rejected, rate limited, timed out, returned a
 * 5xx, or returned output ContractForge couldn't parse into the scenario
 * contract. The intended HTTP status is carried on the exception itself since
 * it varies by failure mode (e.g. 429 for rate limiting vs 502 for a generic
 * upstream failure), unlike the fixed-status exceptions elsewhere in this
 * package.
 */
public class LlmCommunicationException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public LlmCommunicationException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public LlmCommunicationException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
