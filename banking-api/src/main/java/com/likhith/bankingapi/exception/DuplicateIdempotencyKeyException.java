package com.likhith.bankingapi.exception;

/**
 * Raised when an Idempotency-Key header has already been used for the same operation.
 * Always mapped to HTTP 409.
 */
public class DuplicateIdempotencyKeyException extends RuntimeException {

    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("A request with Idempotency-Key '" + idempotencyKey + "' has already been processed");
    }
}
