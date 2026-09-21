package com.likhith.contractforge.executor.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The full record of one execution run against one scenario artifact. Written
 * to disk after every scenario (not just at the end), so a report always
 * reflects real progress even if a later scenario in the same run fails hard.
 */
public record TestRunReport(
        UUID artifactId,
        String endpointId,
        String targetBaseUrl,
        Instant startedAt,
        Instant completedAt,
        int total,
        int passed,
        int failed,
        List<ScenarioExecutionResult> results) {
}
