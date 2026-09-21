package com.likhith.contractforge.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScenarioGenerationResponse(
        String endpointId,
        String model,
        List<ApiScenario> acceptedScenarios,
        List<ScenarioRejection> rejectedScenarios,
        int acceptedCount,
        int rejectedCount,
        Instant generatedAt,
        UUID artifactId,
        String artifactPath) {
}
