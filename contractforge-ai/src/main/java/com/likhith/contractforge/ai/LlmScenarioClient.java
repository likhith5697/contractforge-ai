package com.likhith.contractforge.ai;

import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.LlmScenarioBatch;

/**
 * Boundary between ContractForge and whatever LLM actually generates scenario
 * payloads. Kept as an interface - with {@link OpenAiResponsesClient} as the
 * only production implementation - so tests can substitute a fake without ever
 * making a real network call.
 */
public interface LlmScenarioClient {

    /**
     * @param endpoint     the trusted parser snapshot for exactly one endpoint
     * @param intent       optional free-text guidance from the caller
     * @param maxScenarios upper bound on how many scenarios to request, already clamped to [1, 8]
     * @return the model's raw, untrusted scenario batch - callers must run it through ScenarioValidator
     */
    LlmScenarioBatch generateScenarios(EndpointSnapshot endpoint, String intent, int maxScenarios);
}
