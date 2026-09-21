package com.likhith.contractforge.model;

import java.time.Instant;
import java.util.List;

public record ScenarioGenerationResponse(
        String endpointId,
        String model,
        List<ApiScenario> acceptedScenarios,
        List<ScenarioRejection> rejectedScenarios,
        int acceptedCount,
        int rejectedCount,
        Instant generatedAt) {
}
