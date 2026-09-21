package com.likhith.contractforge.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likhith.contractforge.executor.model.ScenarioExecutionResult;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.ScenarioArtifact;
import com.likhith.contractforge.model.ScenarioCategory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Exercises ScenarioHttpExecutor against a real local HTTP server (the JDK's
 * built-in com.sun.net.httpserver.HttpServer - no extra dependency, no real
 * banking-api needed) so the request it sends, and how it handles the
 * response, can be verified precisely.
 */
class ScenarioHttpExecutorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpServer server;
    private CapturingHandler handler;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        handler = new CapturingHandler();
        server.createContext("/api/payments/scheduled", handler);
        server.start();
        port = server.getAddress().getPort();
        System.setProperty("contractforge.execution.allowed-base-urls", "http://localhost:" + port);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        System.clearProperty("contractforge.execution.allowed-base-urls");
        System.clearProperty("contractforge.execution.connect-timeout-ms");
        System.clearProperty("contractforge.execution.read-timeout-ms");
    }

    @Test
    void sendsTheArtifactsMethodAndPathWithTheScenarioPayloadAndARequestId() {
        handler.responseStatus = 201;
        ScenarioArtifact artifact = artifactWith(scenario("valid_payment", ScenarioCategory.HAPPY_PATH, 201));

        ScenarioHttpExecutor executor = new ScenarioHttpExecutor("http://localhost:" + port);
        ScenarioExecutionResult result = executor.executeAndAssert(artifact, artifact.acceptedScenarios().get(0),
                r -> { });

        assertThat(handler.receivedMethod.get()).isEqualTo("POST");
        assertThat(handler.receivedPath.get()).isEqualTo("/api/payments/scheduled");
        assertThat(handler.receivedContentType.get()).startsWith("application/json");
        assertThat(handler.receivedRequestId.get()).isEqualTo(result.requestId());
        assertThat(handler.receivedRequestId.get()).isNotBlank();

        JsonNode sentBody = parse(handler.receivedBody.get());
        assertThat(sentBody.path("accountId").asText()).isEqualTo("ACC-TEST-001");
    }

    @Test
    void matchingStatusProducesAPassedResult() {
        handler.responseStatus = 201;
        ScenarioArtifact artifact = artifactWith(scenario("valid_payment", ScenarioCategory.HAPPY_PATH, 201));

        ScenarioHttpExecutor executor = new ScenarioHttpExecutor("http://localhost:" + port);
        AtomicReference<ScenarioExecutionResult> captured = new AtomicReference<>();

        executor.executeAndAssert(artifact, artifact.acceptedScenarios().get(0), captured::set);

        assertThat(captured.get().passed()).isTrue();
        assertThat(captured.get().actualStatus()).isEqualTo(201);
        assertThat(captured.get().expectedStatus()).isEqualTo(201);
    }

    @Test
    void mismatchedStatusThrowsAnAssertionErrorWithEvidenceAndStillReportsTheResult() {
        handler.responseStatus = 500;
        ApiScenario scenario = scenario("valid_payment", ScenarioCategory.HAPPY_PATH, 201);
        ScenarioArtifact artifact = artifactWith(scenario);

        ScenarioHttpExecutor executor = new ScenarioHttpExecutor("http://localhost:" + port);
        AtomicReference<ScenarioExecutionResult> captured = new AtomicReference<>();

        assertThatThrownBy(() -> executor.executeAndAssert(artifact, scenario, captured::set))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("valid_payment")
                .hasMessageContaining(artifact.endpointId())
                .hasMessageContaining("201")
                .hasMessageContaining("500")
                .hasMessageContaining(captured.get().requestId());

        // The result must have been reported to the collector before the throw.
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().passed()).isFalse();
    }

    @Test
    void nonAllowlistedBaseUrlIsRejectedBeforeAnyHttpCallIsMade() {
        System.clearProperty("contractforge.execution.allowed-base-urls"); // restore real default allowlist

        assertThatThrownBy(() -> new ScenarioHttpExecutor("https://production.example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in the allowed list");

        assertThat(handler.receivedMethod.get()).isNull();
    }

    private JsonNode parse(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException ex) {
            throw new UncheckedExceptionForTest(ex);
        }
    }

    private ApiScenario scenario(String name, ScenarioCategory category, int expectedStatus) {
        JsonNode payload = OBJECT_MAPPER.valueToTree(java.util.Map.of(
                "accountId", "ACC-TEST-001",
                "billerId", "BLR-TEST-4471"));
        return new ApiScenario(name, category, "test", null, payload, expectedStatus);
    }

    private ScenarioArtifact artifactWith(ApiScenario scenario) {
        return new ScenarioArtifact(UUID.randomUUID(), Instant.now(), "POST:/api/payments/scheduled", "POST",
                "/api/payments/scheduled", List.of(scenario));
    }

    private static class UncheckedExceptionForTest extends RuntimeException {
        UncheckedExceptionForTest(Throwable cause) {
            super(cause);
        }
    }

    private static class CapturingHandler implements HttpHandler {
        volatile int responseStatus = 200;
        final AtomicReference<String> receivedMethod = new AtomicReference<>();
        final AtomicReference<String> receivedPath = new AtomicReference<>();
        final AtomicReference<String> receivedBody = new AtomicReference<>();
        final AtomicReference<String> receivedRequestId = new AtomicReference<>();
        final AtomicReference<String> receivedContentType = new AtomicReference<>();
        final AtomicInteger requestCount = new AtomicInteger(0);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();
            receivedMethod.set(exchange.getRequestMethod());
            receivedPath.set(exchange.getRequestURI().getPath());
            receivedRequestId.set(exchange.getRequestHeaders().getFirst("X-Request-Id"));
            receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] responseBytes = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, responseBytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(responseBytes);
            }
        }
    }
}
