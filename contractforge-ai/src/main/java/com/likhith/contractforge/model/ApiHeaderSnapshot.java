package com.likhith.contractforge.model;

/**
 * A single required header parameter declared on an operation.
 * Optional headers (e.g. Idempotency-Key) are intentionally never represented
 * here - only headers that must be sent are of interest to downstream scenario
 * generation, so {@code required} is always {@code true} for instances emitted
 * by the parser.
 */
public record ApiHeaderSnapshot(
        String name,
        boolean required,
        String type,
        String description) {
}
