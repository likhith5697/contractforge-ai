package com.likhith.contractforge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likhith.contractforge.config.ArtifactProperties;
import com.likhith.contractforge.config.ExecutionProperties;
import com.likhith.contractforge.exception.LlmNotConfiguredException;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.PipelineEndpointResult;
import com.likhith.contractforge.model.PipelineEndpointResult.ExecutionOutcome;
import com.likhith.contractforge.model.PipelineEndpointResult.GenerationOutcome;
import com.likhith.contractforge.model.PipelineRunReport;
import com.likhith.contractforge.model.PipelineRunRequest;
import com.likhith.contractforge.model.ScenarioExecutionOutcome;
import com.likhith.contractforge.model.ScenarioGenerationRequest;
import com.likhith.contractforge.model.ScenarioGenerationResponse;

import lombok.RequiredArgsConstructor;

/**
 * The one-call "do everything" orchestrator: parse the configured OpenAPI
 * source, generate and validate scenarios for every discovered endpoint, then
 * execute each endpoint's accepted scenarios against a validated Test API
 * target - returning (and persisting) one consolidated report.
 *
 * <p>A failure generating scenarios for one endpoint (e.g. a transient OpenAI
 * error) is recorded on that endpoint's result and the run continues with the
 * next endpoint - with up to 20 sequential LLM calls, an isolated hiccup
 * should not abort the whole batch. The one exception is a missing API key:
 * that's fatal for every endpoint, so it's allowed to fail the whole request
 * immediately rather than repeating the same failure 20 times.
 */
@Service
@RequiredArgsConstructor
public class PipelineOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(PipelineOrchestrationService.class);

    private static final String DEFAULT_TARGET_BASE_URL = "http://localhost:8080";
    private static final int DEFAULT_MAX_SCENARIOS_PER_ENDPOINT = 3;
    private static final int MIN_SCENARIOS = 1;
    private static final int MAX_SCENARIOS = 8;
    private static final String DEFAULT_INTENT =
            "Generate a representative mix of happy path, negative, boundary and business-risk scenarios";

    private final ContractParseService contractParseService;
    private final ScenarioGenerationService scenarioGenerationService;
    private final ScenarioExecutionClient scenarioExecutionClient;
    private final ExecutionProperties executionProperties;
    private final ArtifactProperties artifactProperties;
    private final ObjectMapper objectMapper;

    public PipelineRunReport runAll(PipelineRunRequest request) {
        String targetBaseUrl = validateBaseUrl(
                request.targetBaseUrl() != null && !request.targetBaseUrl().isBlank()
                        ? request.targetBaseUrl() : DEFAULT_TARGET_BASE_URL);
        String intent = request.intent() != null && !request.intent().isBlank() ? request.intent() : DEFAULT_INTENT;
        int maxScenarios = clamp(request.maxScenariosPerEndpoint() != null
                ? request.maxScenariosPerEndpoint() : DEFAULT_MAX_SCENARIOS_PER_ENDPOINT);

        UUID runId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        ContractParseResult parseResult = contractParseService.parseConfiguredSource();
        log.info("Pipeline run {} started: {} endpoint(s) discovered, target={}", runId,
                parseResult.postEndpointCount(), targetBaseUrl);

        List<PipelineEndpointResult> endpointResults = new ArrayList<>();
        int totalExecuted = 0;
        int totalPassed = 0;
        int endpointsWithAccepted = 0;

        for (EndpointSnapshot endpoint : parseResult.endpoints()) {
            PipelineEndpointResult result = runForEndpoint(endpoint, intent, maxScenarios, targetBaseUrl);
            endpointResults.add(result);
            totalExecuted += result.execution().total();
            totalPassed += result.execution().passed();
            if (result.generation().acceptedCount() > 0) {
                endpointsWithAccepted++;
            }
        }

        PipelineRunReport report = new PipelineRunReport(runId, startedAt, Instant.now(), targetBaseUrl,
                parseResult.postEndpointCount(), endpointsWithAccepted, totalExecuted, totalPassed,
                totalExecuted - totalPassed, endpointResults);

        log.info("Pipeline run {} completed: {} endpoint(s), {} scenario(s) executed ({} passed, {} failed)",
                runId, parseResult.postEndpointCount(), totalExecuted, totalPassed, totalExecuted - totalPassed);

        writeReportSafely(report);
        return report;
    }

    private PipelineEndpointResult runForEndpoint(EndpointSnapshot endpoint, String intent, int maxScenarios,
            String targetBaseUrl) {
        ScenarioGenerationResponse generation;
        try {
            generation = scenarioGenerationService.generate(
                    new ScenarioGenerationRequest(endpoint.endpointId(), intent, maxScenarios));
        } catch (LlmNotConfiguredException ex) {
            throw ex; // fatal for the whole run, not just this endpoint
        } catch (Exception ex) {
            log.warn("Generation failed for {}: {}", endpoint.endpointId(), ex.getMessage());
            return new PipelineEndpointResult(endpoint.endpointId(),
                    new GenerationOutcome("FAILED", null, 0, 0, null, null, ex.getMessage()),
                    new ExecutionOutcome("SKIPPED", 0, 0, 0, List.of(), "Generation failed"));
        }

        GenerationOutcome generationOutcome = new GenerationOutcome("OK", generation.model(),
                generation.acceptedCount(), generation.rejectedCount(), generation.artifactId(),
                generation.artifactPath(), null);

        if (generation.acceptedScenarios().isEmpty()) {
            return new PipelineEndpointResult(endpoint.endpointId(), generationOutcome,
                    new ExecutionOutcome("SKIPPED_NO_ACCEPTED_SCENARIOS", 0, 0, 0, List.of(), null));
        }

        List<ScenarioExecutionOutcome> outcomes = new ArrayList<>();
        for (ApiScenario scenario : generation.acceptedScenarios()) {
            outcomes.add(scenarioExecutionClient.execute(targetBaseUrl, endpoint, scenario));
        }
        int passed = (int) outcomes.stream().filter(ScenarioExecutionOutcome::passed).count();

        return new PipelineEndpointResult(endpoint.endpointId(), generationOutcome,
                new ExecutionOutcome("OK", outcomes.size(), passed, outcomes.size() - passed, outcomes, null));
    }

    private String validateBaseUrl(String candidate) {
        String normalized = candidate.endsWith("/") ? candidate.substring(0, candidate.length() - 1) : candidate;
        if (!executionProperties.getAllowedBaseUrls().contains(normalized)) {
            throw new IllegalArgumentException("Target base URL '" + candidate + "' is not in the allowed list "
                    + executionProperties.getAllowedBaseUrls() + "; refusing to run the pipeline against it.");
        }
        return normalized;
    }

    private int clamp(int requested) {
        return Math.max(MIN_SCENARIOS, Math.min(MAX_SCENARIOS, requested));
    }

    private void writeReportSafely(PipelineRunReport report) {
        try {
            Path dir = Path.of(artifactProperties.getReportDirectory());
            Files.createDirectories(dir);
            Path file = dir.resolve("pipeline-" + report.runId() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), report);
            log.info("Wrote pipeline report to {}", file);
        } catch (IOException ex) {
            log.warn("Failed to write pipeline report to disk (response is unaffected): {}", ex.getMessage());
        }
    }
}
