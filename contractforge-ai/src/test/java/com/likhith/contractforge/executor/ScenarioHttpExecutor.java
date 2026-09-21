package com.likhith.contractforge.executor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.likhith.contractforge.executor.model.ScenarioExecutionResult;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.ScenarioArtifact;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Sends exactly one real HTTP request per scenario, using only trusted
 * inputs: the base URL passed in (validated against an explicit allowlist
 * before anything else happens) and the method/path recorded on the artifact
 * at generation time. The scenario's payload is used only as the request
 * body - it can never redirect the call, change the method, or inject
 * headers.
 *
 * <p>Reads its safety configuration ({@code contractforge.execution.*}) from
 * JVM system properties with defaults matching {@code application.yml}'s
 * documented ones, since this class runs as a plain JUnit test with no Spring
 * context - see {@link com.likhith.contractforge.config.ExecutionProperties}
 * for the Spring-side documentation of the same settings.
 */
public class ScenarioHttpExecutor {

    private static final Logger log = LoggerFactory.getLogger(ScenarioHttpExecutor.class);

    private static final List<String> DEFAULT_ALLOWED_BASE_URLS =
            List.of("http://localhost:8080", "http://127.0.0.1:8080");
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 15000;

    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public ScenarioHttpExecutor(String candidateBaseUrl) {
        List<String> allowedBaseUrls = resolveAllowedBaseUrls();
        this.baseUrl = validateBaseUrl(candidateBaseUrl, allowedBaseUrls);
        this.connectTimeoutMs = intSystemProperty("contractforge.execution.connect-timeout-ms",
                DEFAULT_CONNECT_TIMEOUT_MS);
        this.readTimeoutMs = intSystemProperty("contractforge.execution.read-timeout-ms", DEFAULT_READ_TIMEOUT_MS);
    }

    /**
     * Sends the request, records the result via {@code onResult} (so it can be
     * collected and the run report written even on failure), then throws if the
     * actual status didn't match. The throw always happens after {@code onResult}
     * has run, so a failing scenario is never lost from the report.
     */
    public ScenarioExecutionResult executeAndAssert(ScenarioArtifact artifact, ApiScenario scenario,
            Consumer<ScenarioExecutionResult> onResult) {
        ScenarioExecutionResult result = execute(artifact, scenario);
        onResult.accept(result);
        if (!result.passed()) {
            throw new AssertionError(failureMessage(artifact, scenario, result));
        }
        return result;
    }

    private ScenarioExecutionResult execute(ScenarioArtifact artifact, ApiScenario scenario) {
        String requestId = UUID.randomUUID().toString();
        String url = joinUrl(baseUrl, artifact.targetPath());
        String method = artifact.targetMethod() == null ? "POST" : artifact.targetMethod();

        long startNanos = System.nanoTime();
        Response response = sendRequest(url, method, scenario, requestId);
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

        int actualStatus = response.getStatusCode();
        boolean passed = actualStatus == scenario.expectedStatus();
        int responseBodySize = response.getBody().asByteArray().length;

        log.info("Executed scenario '{}' [{} {}] expected={} actual={} durationMs={} responseBodyBytes={} requestId={}",
                scenario.name(), method, artifact.targetPath(), scenario.expectedStatus(), actualStatus, durationMs,
                responseBodySize, requestId);

        return new ScenarioExecutionResult(scenario.name(), scenario.expectedStatus(), actualStatus, passed,
                requestId, durationMs);
    }

    private Response sendRequest(String url, String method, ApiScenario scenario, String requestId) {
        RequestSpecification request = RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectTimeoutMs)
                        .setParam("http.socket.timeout", readTimeoutMs)))
                .contentType(ContentType.JSON)
                .header("X-Request-Id", requestId)
                .body(scenario.payload().toString());

        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> request.post(url);
            case "PUT" -> request.put(url);
            case "PATCH" -> request.patch(url);
            case "DELETE" -> request.delete(url);
            case "GET" -> request.get(url);
            default -> throw new IllegalArgumentException("Unsupported HTTP method in scenario artifact: " + method);
        };
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = stripTrailingSlash(baseUrl);
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    private String validateBaseUrl(String candidateBaseUrl, List<String> allowedBaseUrls) {
        if (candidateBaseUrl == null || candidateBaseUrl.isBlank()) {
            throw new IllegalArgumentException("contractforge.test-base-url must be set");
        }
        String normalized = stripTrailingSlash(candidateBaseUrl);
        if (!allowedBaseUrls.contains(normalized)) {
            throw new IllegalArgumentException("Target base URL '" + candidateBaseUrl
                    + "' is not in the allowed list " + allowedBaseUrls + "; refusing to execute any scenario "
                    + "against it. This harness never targets production or arbitrary hosts.");
        }
        return normalized;
    }

    private List<String> resolveAllowedBaseUrls() {
        String override = System.getProperty("contractforge.execution.allowed-base-urls");
        if (override == null || override.isBlank()) {
            return DEFAULT_ALLOWED_BASE_URLS;
        }
        return Arrays.stream(override.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::stripTrailingSlash)
                .toList();
    }

    private String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private int intSystemProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            log.warn("Invalid integer for system property '{}': '{}'; using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private String failureMessage(ScenarioArtifact artifact, ApiScenario scenario, ScenarioExecutionResult result) {
        return "Scenario '%s' for endpoint '%s' expected HTTP %d but got %d (requestId=%s, durationMs=%d)"
                .formatted(scenario.name(), artifact.endpointId(), result.expectedStatus(), result.actualStatus(),
                        result.requestId(), result.durationMs());
    }
}
