package com.likhith.contractforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likhith.contractforge.ai.FakeLlmScenarioClient;
import com.likhith.contractforge.config.ArtifactProperties;
import com.likhith.contractforge.config.ExecutionProperties;
import com.likhith.contractforge.config.OpenAiProperties;
import com.likhith.contractforge.config.OpenApiSourceProperties;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.LlmScenarioBatch;
import com.likhith.contractforge.model.PipelineRunReport;
import com.likhith.contractforge.model.PipelineRunRequest;
import com.likhith.contractforge.model.ScenarioCategory;
import com.likhith.contractforge.parser.OpenApiContractParser;
import com.likhith.contractforge.parser.SchemaFlattener;
import com.sun.net.httpserver.HttpServer;

/**
 * Drives PipelineOrchestrationService end to end: real fixture parsing, a
 * fake LLM client (no OpenAI call), and a real local HTTP server standing in
 * for banking-api (no network dependency, deterministic).
 */
class PipelineOrchestrationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private FakeLlmScenarioClient llmScenarioClient;
    private HttpServer server;
    private int serverPort;

    @TempDir
    private Path scenarioDir;
    @TempDir
    private Path reportDir;

    @BeforeEach
    void setUp() throws IOException {
        llmScenarioClient = new FakeLlmScenarioClient();

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/payments/scheduled", exchange -> {
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(201, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        serverPort = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private PipelineOrchestrationService orchestrator(List<String> allowedBaseUrls) {
        ContractSnapshotCache cache = new ContractSnapshotCache();
        OpenApiFetcher fetcher = mock(OpenApiFetcher.class);
        when(fetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());
        OpenApiSourceProperties sourceProperties = new OpenApiSourceProperties();
        sourceProperties.setSourceUrl("http://localhost:8080/v3/api-docs");

        ContractParseService parseService = new ContractParseService(sourceProperties, fetcher,
                new OpenApiContractParser(new SchemaFlattener()), cache);

        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("sk-test-key");
        openAiProperties.setModel("gpt-5-mini");

        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setScenarioDirectory(scenarioDir.toString());
        artifactProperties.setReportDirectory(reportDir.toString());

        ScenarioGenerationService generationService = new ScenarioGenerationService(cache, openAiProperties,
                llmScenarioClient, new ScenarioValidator(),
                new FileSystemScenarioArtifactStore(artifactProperties, objectMapper));

        ExecutionProperties executionProperties = new ExecutionProperties();
        executionProperties.setAllowedBaseUrls(allowedBaseUrls);

        ScenarioExecutionClient executionClient = new ScenarioExecutionClient(RestClient.create());

        return new PipelineOrchestrationService(parseService, generationService, executionClient,
                executionProperties, artifactProperties, objectMapper);
    }

    @Test
    void runsGenerationAndExecutionForEveryDiscoveredEndpointAndWritesAReport() throws IOException {
        JsonNode payload = objectMapper.valueToTree(Map.of(
                "accountId", "ACC-TEST-001",
                "billerId", "BLR-TEST-4471",
                "billerCategory", "ELECTRICITY",
                "consumerNumber", "CONS-TEST-99001122",
                "amount", Map.of("value", 125.50, "currency", "USD"),
                "scheduleDetails", Map.of("startDate", "2026-10-01", "frequency", "MONTHLY"),
                "autoPayEnabled", true));
        ApiScenario happyPath = new ApiScenario("valid_payment", ScenarioCategory.HAPPY_PATH, "test", null, payload, 201);
        llmScenarioClient.willReturn(new LlmScenarioBatch("POST:/api/payments/scheduled", List.of(happyPath)));

        String targetBaseUrl = "http://localhost:" + serverPort;
        PipelineOrchestrationService orchestrator = orchestrator(List.of(targetBaseUrl));

        PipelineRunReport report = orchestrator.runAll(new PipelineRunRequest(null, 3, targetBaseUrl));

        assertThat(report.totalEndpoints()).isEqualTo(1);
        assertThat(report.endpointsWithAcceptedScenarios()).isEqualTo(1);
        assertThat(report.totalScenariosExecuted()).isEqualTo(1);
        assertThat(report.totalScenariosPassed()).isEqualTo(1);
        assertThat(report.totalScenariosFailed()).isZero();

        var endpointResult = report.endpoints().get(0);
        assertThat(endpointResult.endpointId()).isEqualTo("POST:/api/payments/scheduled");
        assertThat(endpointResult.generation().status()).isEqualTo("OK");
        assertThat(endpointResult.generation().acceptedCount()).isEqualTo(1);
        assertThat(endpointResult.execution().status()).isEqualTo("OK");
        assertThat(endpointResult.execution().results()).hasSize(1);
        assertThat(endpointResult.execution().results().get(0).passed()).isTrue();

        // The report must also have been persisted to disk.
        try (var files = Files.list(reportDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    void rejectsANonAllowlistedTargetBaseUrlBeforeDoingAnyWork() {
        PipelineOrchestrationService orchestrator = orchestrator(List.of("http://localhost:8080"));

        assertThatThrownBy(() -> orchestrator.runAll(new PipelineRunRequest(null, null, "https://evil.example.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in the allowed list");
    }

    private String fixtureJson() {
        try {
            Path path = new ClassPathResource("scheduled-payment-openapi-fixture.json").getFile().toPath();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }
}
