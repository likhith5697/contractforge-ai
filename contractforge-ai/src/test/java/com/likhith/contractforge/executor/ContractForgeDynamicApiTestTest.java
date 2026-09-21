package com.likhith.contractforge.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.likhith.contractforge.executor.model.ScenarioExecutionResult;
import com.likhith.contractforge.executor.model.TestRunReport;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.ScenarioArtifact;
import com.likhith.contractforge.model.ScenarioCategory;
import com.sun.net.httpserver.HttpServer;

/**
 * End-to-end test of the harness factory itself: it reads an artifact file
 * and system properties exactly as {@code mvn test -Dcontractforge...} would
 * supply them, and drives a real local server through
 * {@link ContractForgeDynamicApiTest#executeAcceptedScenarios()}.
 */
class ContractForgeDynamicApiTestTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final String SECRET_LOOKING_ACCOUNT_ID = "ACC-SECRET-VALUE-12345";

    private HttpServer server;

    @AfterEach
    void cleanUp() {
        if (server != null) {
            server.stop(0);
        }
        System.clearProperty("contractforge.scenario-artifact");
        System.clearProperty("contractforge.test-base-url");
        System.clearProperty("contractforge.execution.allowed-base-urls");
        System.clearProperty("contractforge.artifacts.report-directory");
    }

    @Test
    void returnsAnEmptyStreamWhenTheArtifactPropertyIsAbsent() {
        System.clearProperty("contractforge.scenario-artifact");

        List<DynamicTest> tests = new ContractForgeDynamicApiTest().executeAcceptedScenarios().toList();

        assertThat(tests).isEmpty();
    }

    @Test
    void executesScenariosInOrderAndWritesAReportWithoutLeakingThePayload(@TempDir Path tempDir) throws Throwable {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/payments/scheduled", exchange -> {
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        int port = server.getAddress().getPort();

        ApiScenario passingScenario = scenario("first_scenario", 500); // server always answers 500 -> matches
        ApiScenario failingScenario = scenario("second_scenario", 201); // -> won't match 500

        ScenarioArtifact artifact = new ScenarioArtifact(UUID.randomUUID(), Instant.now(),
                "POST:/api/payments/scheduled", "POST", "/api/payments/scheduled",
                List.of(passingScenario, failingScenario));

        Path artifactFile = tempDir.resolve("artifact.json");
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(artifactFile.toFile(), artifact);

        Path reportsDir = tempDir.resolve("reports");
        System.setProperty("contractforge.scenario-artifact", artifactFile.toString());
        System.setProperty("contractforge.test-base-url", "http://localhost:" + port);
        System.setProperty("contractforge.execution.allowed-base-urls", "http://localhost:" + port);
        System.setProperty("contractforge.artifacts.report-directory", reportsDir.toString());

        List<DynamicTest> tests = new ContractForgeDynamicApiTest().executeAcceptedScenarios().toList();
        assertThat(tests).hasSize(2);
        assertThat(tests).extracting(DynamicTest::getDisplayName)
                .containsExactly("first_scenario", "second_scenario");

        tests.get(0).getExecutable().execute();
        assertThatThrownBy(() -> tests.get(1).getExecutable().execute()).isInstanceOf(AssertionError.class);

        List<Path> reportFiles;
        try (var stream = Files.list(reportsDir)) {
            reportFiles = stream.toList();
        }
        assertThat(reportFiles).hasSize(1);

        String reportJson = Files.readString(reportFiles.get(0));
        assertThat(reportJson).doesNotContain(SECRET_LOOKING_ACCOUNT_ID);

        TestRunReport report = OBJECT_MAPPER.readValue(reportFiles.get(0).toFile(), TestRunReport.class);
        assertThat(report.total()).isEqualTo(2);
        assertThat(report.passed()).isEqualTo(1);
        assertThat(report.failed()).isEqualTo(1);
        assertThat(report.results()).extracting(ScenarioExecutionResult::scenarioName)
                .containsExactly("first_scenario", "second_scenario");
        assertThat(report.results()).extracting(ScenarioExecutionResult::requestId).allMatch(id -> !id.isBlank());
    }

    private ApiScenario scenario(String name, int expectedStatus) {
        JsonNode payload = OBJECT_MAPPER.valueToTree(Map.of("accountId", SECRET_LOOKING_ACCOUNT_ID));
        return new ApiScenario(name, ScenarioCategory.HAPPY_PATH, "test", null, payload, expectedStatus);
    }
}
