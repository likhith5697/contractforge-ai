package com.likhith.contractforge.exception;

/**
 * Raised when snapshots are requested before any successful parse has run.
 * Always mapped to HTTP 409 (Conflict) per the API contract.
 */
public class SnapshotsNotAvailableException extends RuntimeException {

    public SnapshotsNotAvailableException(String message) {
        super(message);
    }
}
