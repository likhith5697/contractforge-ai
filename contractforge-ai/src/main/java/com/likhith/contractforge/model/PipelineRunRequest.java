package com.likhith.contractforge.model;

import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/pipeline/run. Every field is optional - an empty
 * body runs the whole pipeline with sane defaults (all discovered endpoints,
 * a generic intent, 3 scenarios each, target http://localhost:8080).
 *
 * <p>{@code targetBaseUrl}, if given, is still validated against the same
 * fixed allowlist the Phase 3 harness uses - a caller can pick which
 * allow-listed target to hit, never an arbitrary one.
 */
public record PipelineRunRequest(
        @Size(max = 500, message = "intent must not exceed 500 characters") String intent,
        Integer maxScenariosPerEndpoint,
        String targetBaseUrl) {
}
