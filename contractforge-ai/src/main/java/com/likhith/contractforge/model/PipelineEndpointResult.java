package com.likhith.contractforge.model;

import java.util.List;
import java.util.UUID;

/**
 * What happened for one endpoint during a pipeline run: how generation went,
 * and (if there were any accepted scenarios) how execution against the Test
 * API went. Each stage's status is independent so a reader can see exactly
 * where things stood without cross-referencing other endpoints.
 */
public record PipelineEndpointResult(
        String endpointId,
        GenerationOutcome generation,
        ExecutionOutcome execution) {

    public record GenerationOutcome(
            String status,
            String model,
            int acceptedCount,
            int rejectedCount,
            UUID artifactId,
            String artifactPath,
            String error) {
    }

    public record ExecutionOutcome(
            String status,
            int total,
            int passed,
            int failed,
            List<ScenarioExecutionOutcome> results,
            String error) {
    }
}
