package com.likhith.contractforge.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One synthetic test scenario for a single endpoint - either as returned raw
 * by the LLM (untrusted) or, once it has passed {@code ScenarioValidator},
 * accepted as-is. The type carries no trust flag; trust is determined entirely
 * by which list ({@code accepted} vs {@code rejected}) it ends up in after
 * validation.
 *
 * <p>{@code payload} is a {@link JsonNode} rather than a Java DTO because the
 * shape is endpoint-specific and dynamic - forcing every banking payload into
 * a compile-time Java class would defeat the point of driving this off the
 * parsed schema.
 */
public record ApiScenario(
        String name,
        ScenarioCategory category,
        String purpose,
        String violatedRulePath,
        JsonNode payload,
        int expectedStatus) {
}
