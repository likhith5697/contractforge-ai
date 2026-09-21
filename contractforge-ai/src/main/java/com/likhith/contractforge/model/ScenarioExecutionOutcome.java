package com.likhith.contractforge.model;

/**
 * The outcome of executing exactly one accepted scenario against the Test
 * API target, from the /api/pipeline/run orchestrator. {@code actualStatus}
 * is null and {@code error} is set when the call itself couldn't complete
 * (e.g. connection refused) rather than returning a real HTTP status - this
 * orchestrator never lets one bad call abort the batch.
 */
public record ScenarioExecutionOutcome(
        String scenarioName,
        int expectedStatus,
        Integer actualStatus,
        boolean passed,
        String requestId,
        long durationMs,
        String error) {
}
