package com.likhith.contractforge.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.ScenarioExecutionOutcome;

import lombok.RequiredArgsConstructor;

/**
 * Sends exactly one real HTTP request per scenario for the /api/pipeline/run
 * orchestrator - the main-app equivalent of the Phase 3 JUnit harness's
 * ScenarioHttpExecutor, built on Spring's RestClient instead of RestAssured
 * (already a main-scope dependency, unlike RestAssured which is test-only).
 *
 * <p>Never throws for a status mismatch or a connection failure - both are
 * captured in the returned outcome, since one bad scenario must never abort
 * a batch run across 20 endpoints. The scenario's payload is used only as the
 * request body; the URL, method, and headers all come from trusted inputs.
 */
@Component
@RequiredArgsConstructor
public class ScenarioExecutionClient {

    private static final Logger log = LoggerFactory.getLogger(ScenarioExecutionClient.class);

    private final RestClient executionRestClient;

    public ScenarioExecutionOutcome execute(String validatedBaseUrl, EndpointSnapshot endpoint, ApiScenario scenario) {
        String requestId = UUID.randomUUID().toString();
        String url = joinUrl(validatedBaseUrl, endpoint.path());
        HttpMethod method = HttpMethod.valueOf(endpoint.method() == null ? "POST" : endpoint.method());

        long startNanos = System.nanoTime();
        try {
            ResponseEntity<Void> response = executionRestClient.method(method)
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-Id", requestId)
                    .body(scenario.payload())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> { /* capture the status, don't throw */ })
                    .toBodilessEntity();

            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            int actualStatus = response.getStatusCode().value();
            boolean passed = actualStatus == scenario.expectedStatus();

            log.info("Executed scenario '{}' [{} {}] expected={} actual={} durationMs={} requestId={}",
                    scenario.name(), method, endpoint.path(), scenario.expectedStatus(), actualStatus, durationMs,
                    requestId);

            return new ScenarioExecutionOutcome(scenario.name(), scenario.expectedStatus(), actualStatus, passed,
                    requestId, durationMs, null);
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.warn("Execution failed for scenario '{}' [{} {}] requestId={}: {}",
                    scenario.name(), method, endpoint.path(), requestId, ex.getMessage());
            return new ScenarioExecutionOutcome(scenario.name(), scenario.expectedStatus(), null, false, requestId,
                    durationMs, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
