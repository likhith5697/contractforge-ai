package com.likhith.contractforge.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The consolidated result of one POST /api/pipeline/run call: parse once,
 * generate for every discovered endpoint, execute every accepted scenario
 * against the validated target, all in one response. Also persisted to
 * {@code <report-directory>/pipeline-<runId>.json}.
 */
public record PipelineRunReport(
        UUID runId,
        Instant startedAt,
        Instant completedAt,
        String targetBaseUrl,
        int totalEndpoints,
        int endpointsWithAcceptedScenarios,
        int totalScenariosExecuted,
        int totalScenariosPassed,
        int totalScenariosFailed,
        List<PipelineEndpointResult> endpoints) {
}
