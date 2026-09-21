package com.likhith.contractforge.exception;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error body returned by every failure path in this API.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        List<String> details) {
}
