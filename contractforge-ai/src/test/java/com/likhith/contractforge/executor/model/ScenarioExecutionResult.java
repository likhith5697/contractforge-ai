package com.likhith.contractforge.executor.model;

/**
 * The outcome of executing exactly one accepted scenario against the test
 * target. Deliberately carries no payload or response body - only what's
 * needed to know whether it passed and to debug why not.
 */
public record ScenarioExecutionResult(
        String scenarioName,
        int expectedStatus,
        int actualStatus,
        boolean passed,
        String requestId,
        long durationMs) {
}
