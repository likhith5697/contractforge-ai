package com.likhith.contractforge.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/scenarios/generate}. {@code maxScenarios}
 * is deliberately not range-annotated here - an out-of-range value is clamped
 * to [1, 8] by the service rather than rejected, since "generate as many as
 * sensible up to my ceiling" is a more useful client experience than a 400.
 */
public record ScenarioGenerationRequest(
        @NotBlank(message = "endpointId is required") String endpointId,
        @Size(max = 500, message = "intent must not exceed 500 characters") String intent,
        Integer maxScenarios) {
}
