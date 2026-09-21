package com.likhith.contractforge.model;

import java.util.List;

/**
 * The raw, untrusted structured-output payload from the LLM, deserialized
 * directly from its JSON text - mirrors the strict response contract exactly
 * ({@code {"endpointId": "...", "scenarios": [...]}}) so no translation layer
 * sits between OpenAI's output and validation.
 */
public record LlmScenarioBatch(
        String endpointId,
        List<ApiScenario> scenarios) {
}
