package com.likhith.contractforge.exception;

/**
 * Raised when a specific endpoint id is not present in the cached snapshot.
 * Always mapped to HTTP 404 (Not Found).
 */
public class SnapshotNotFoundException extends RuntimeException {

    public SnapshotNotFoundException(String message) {
        super(message);
    }
}
