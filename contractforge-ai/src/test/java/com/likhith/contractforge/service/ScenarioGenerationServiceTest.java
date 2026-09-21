package com.likhith.contractforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.likhith.contractforge.ai.FakeLlmScenarioClient;
import com.likhith.contractforge.config.ArtifactProperties;
import com.likhith.contractforge.config.OpenAiProperties;
import com.likhith.contractforge.config.OpenApiSourceProperties;
import com.likhith.contractforge.exception.LlmCommunicationException;
import com.likhith.contractforge.exception.LlmNotConfiguredException;
import com.likhith.contractforge.exception.SnapshotNotFoundException;
import com.likhith.contractforge.exception.SnapshotsNotAvailableException;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.LlmScenarioBatch;
import com.likhith.contractforge.model.ScenarioArtifact;
import com.likhith.contractforge.model.ScenarioCategory;
import com.likhith.contractforge.model.ScenarioGenerationRequest;
import com.likhith.contractforge.model.ScenarioGenerationResponse;
import com.likhith.contractforge.parser.OpenApiContractParser;
import com.likhith.contractforge.parser.SchemaFlattener;

/**
 * Drives ScenarioGenerationService against the same OpenAPI fixture used for
 * the parser tests, with a fake LLM client standing in for OpenAI - no real
 * network call is ever made, and the real ScenarioValidator does the actual
 * work of accepting/rejecting scenarios.
 */
class ScenarioGenerationServiceTest {

    private static final String ENDPOINT_ID = "POST:/api/payments/scheduled";

    @TempDir
    private Path scenarioDirectory;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private FakeLlmScenarioClient llmScenarioClient;
    private OpenAiProperties openAiProperties;
    private ContractSnapshotCache populatedCache;
    private FileSystemScenarioArtifactStore artifactStore;

    @BeforeEach
    void setUp() {
        llmScenarioClient = new FakeLlmScenarioClient();

        openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("sk-test-key");
        openAiProperties.setModel("gpt-5-mini");

        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setScenarioDirectory(scenarioDirectory.toString());
        artifactStore = new FileSystemScenarioArtifactStore(artifactProperties, objectMapper);

        populatedCache = new ContractSnapshotCache();
        OpenApiFetcher fetcher = mock(OpenApiFetcher.class);
        when(fetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());
        OpenApiSourceProperties sourceProperties = new OpenApiSourceProperties();
        sourceProperties.setSourceUrl("http://localhost:8080/v3/api-docs");
        new ContractParseService(sourceProperties, fetcher, new OpenApiContractParser(new SchemaFlattener()),
                populatedCache)
                .parseConfiguredSource();
    }

    private ScenarioGenerationService serviceWith(ContractSnapshotCache cache) {
        return new ScenarioGenerationService(cache, openAiProperties, llmScenarioClient, new ScenarioValidator(),
                artifactStore);
    }

    @Test
    void failsWith409WhenParserCacheIsEmpty() {
        ScenarioGenerationService service = serviceWith(new ContractSnapshotCache());

        assertThatThrownBy(() -> service.generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null)))
                .isInstanceOf(SnapshotsNotAvailableException.class);
    }

    @Test
    void failsWith404ForUnknownEndpointId() {
        ScenarioGenerationService service = serviceWith(populatedCache);

        assertThatThrownBy(() -> service.generate(
                new ScenarioGenerationRequest("POST:/api/does-not-exist", null, null)))
                .isInstanceOf(SnapshotNotFoundException.class);
    }

    @Test
    void failsWith503WhenApiKeyIsMissing() {
        openAiProperties.setApiKey("");
        ScenarioGenerationService service = serviceWith(populatedCache);

        assertThatThrownBy(() -> service.generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null)))
                .isInstanceOf(LlmNotConfiguredException.class);
    }

    @Test
    void acceptsAValidHappyPathAndAValidNegativeScenario() {
        ApiScenario happyPath = scenario("valid_monthly_electricity_payment", ScenarioCategory.HAPPY_PATH,
                null, 201, validHappyPathPayload());
        ApiScenario negative = scenario("reject_excessive_notify_before_days", ScenarioCategory.NEGATIVE,
                "notifyBeforeDays", 400, payloadWithNotifyBeforeDays(999));

        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(happyPath, negative)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, "cover happy path and boundary", 6));

        assertThat(response.acceptedCount()).isEqualTo(2);
        assertThat(response.rejectedCount()).isZero();
        assertThat(response.model()).isEqualTo("gpt-5-mini");
        assertThat(response.endpointId()).isEqualTo(ENDPOINT_ID);
    }

    @Test
    void persistsAScenarioArtifactAfterAcceptedGeneration() throws IOException {
        ApiScenario happyPath = scenario("valid_monthly_electricity_payment", ScenarioCategory.HAPPY_PATH,
                null, 201, validHappyPathPayload());
        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(happyPath)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.artifactId()).isNotNull();
        assertThat(response.artifactPath()).isNotBlank();

        Path artifactFile = Path.of(response.artifactPath());
        assertThat(Files.exists(artifactFile)).isTrue();

        ScenarioArtifact persisted = objectMapper.readValue(artifactFile.toFile(), ScenarioArtifact.class);
        assertThat(persisted.artifactId()).isEqualTo(response.artifactId());
        assertThat(persisted.endpointId()).isEqualTo(ENDPOINT_ID);
        assertThat(persisted.targetMethod()).isEqualTo("POST");
        assertThat(persisted.targetPath()).isEqualTo("/api/payments/scheduled");
        assertThat(persisted.acceptedScenarios()).hasSize(1);
        assertThat(persisted.acceptedScenarios().get(0).name()).isEqualTo("valid_monthly_electricity_payment");
    }

    @Test
    void artifactExcludesRejectedScenarios() throws IOException {
        ApiScenario happyPath = scenario("valid_monthly_electricity_payment", ScenarioCategory.HAPPY_PATH,
                null, 201, validHappyPathPayload());
        ApiScenario invalidEnum = scenario("invalid_biller_category", ScenarioCategory.HAPPY_PATH, null, 201,
                withInvalidEnum());

        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(happyPath, invalidEnum)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.acceptedCount()).isEqualTo(1);
        assertThat(response.rejectedCount()).isEqualTo(1);

        ScenarioArtifact persisted = objectMapper.readValue(Path.of(response.artifactPath()).toFile(),
                ScenarioArtifact.class);
        assertThat(persisted.acceptedScenarios()).hasSize(1);
        assertThat(persisted.acceptedScenarios())
                .noneMatch(s -> s.name().equals("invalid_biller_category"));
    }

    @Test
    void rejectsAScenarioWithAnUnknownPayloadField() {
        var payload = (ObjectNode) validHappyPathPayload();
        payload.put("notARealField", "surprise");

        ApiScenario scenario = scenario("has_unknown_field", ScenarioCategory.HAPPY_PATH, null, 201, payload);
        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(scenario)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.acceptedCount()).isZero();
        assertThat(response.rejectedCount()).isEqualTo(1);
        assertThat(response.rejectedScenarios().get(0).reasons())
                .anyMatch(reason -> reason.contains("Unknown field 'notARealField'"));
    }

    @Test
    void rejectsAHappyPathScenarioMissingARequiredField() {
        var payload = (ObjectNode) validHappyPathPayload();
        payload.remove("billerId");

        ApiScenario scenario = scenario("missing_biller_id", ScenarioCategory.HAPPY_PATH, null, 201, payload);
        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(scenario)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.acceptedCount()).isZero();
        assertThat(response.rejectedScenarios().get(0).reasons())
                .anyMatch(reason -> reason.contains("Missing required field 'billerId'"));
    }

    @Test
    void rejectsAHappyPathScenarioWithAnInvalidEnumValue() {
        var payload = (ObjectNode) validHappyPathPayload();
        payload.put("billerCategory", "NOT_A_REAL_CATEGORY");

        ApiScenario scenario = scenario("invalid_biller_category", ScenarioCategory.HAPPY_PATH, null, 201, payload);
        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(scenario)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.acceptedCount()).isZero();
        assertThat(response.rejectedScenarios().get(0).reasons())
                .anyMatch(reason -> reason.contains("not one of the allowed values"));
    }

    @Test
    void rejectsAScenarioWithAnUndocumentedExpectedStatus() {
        ApiScenario scenario = scenario("wrong_expected_status", ScenarioCategory.HAPPY_PATH, null, 999,
                validHappyPathPayload());
        llmScenarioClient.willReturn(new LlmScenarioBatch(ENDPOINT_ID, List.of(scenario)));

        ScenarioGenerationResponse response = serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null));

        assertThat(response.acceptedCount()).isZero();
        assertThat(response.rejectedScenarios().get(0).reasons())
                .anyMatch(reason -> reason.contains("is not a documented success status code"));
    }

    @Test
    void malformedLlmResponseMapsToAClearFailure() {
        llmScenarioClient.willThrow(new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_MALFORMED_RESPONSE",
                "OpenAI's structured output could not be parsed into the scenario contract"));

        assertThatThrownBy(() -> serviceWith(populatedCache)
                .generate(new ScenarioGenerationRequest(ENDPOINT_ID, null, null)))
                .isInstanceOf(LlmCommunicationException.class)
                .hasMessageContaining("could not be parsed");
    }

    private ApiScenario scenario(String name, ScenarioCategory category, String violatedRulePath, int expectedStatus,
            JsonNode payload) {
        return new ApiScenario(name, category, "test scenario", violatedRulePath, payload, expectedStatus);
    }

    private JsonNode validHappyPathPayload() {
        return objectMapper.valueToTree(Map.of(
                "accountId", "ACC-TEST-001",
                "billerId", "BLR-TEST-4471",
                "billerCategory", "ELECTRICITY",
                "consumerNumber", "CONS-TEST-99001122",
                "amount", Map.of("value", 125.50, "currency", "USD"),
                "scheduleDetails", Map.of("startDate", "2026-10-01", "frequency", "MONTHLY"),
                "autoPayEnabled", true,
                "notifyBeforeDays", 3));
    }

    private JsonNode payloadWithNotifyBeforeDays(int notifyBeforeDays) {
        var payload = (ObjectNode) validHappyPathPayload();
        payload.put("notifyBeforeDays", notifyBeforeDays);
        return payload;
    }

    private JsonNode withInvalidEnum() {
        var payload = (ObjectNode) validHappyPathPayload();
        payload.put("billerCategory", "NOT_A_REAL_CATEGORY");
        return payload;
    }

    private String fixtureJson() {
        try {
            Path path = new ClassPathResource("scheduled-payment-openapi-fixture.json").getFile().toPath();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
