package com.likhith.contractforge.service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.likhith.contractforge.ai.LlmScenarioClient;
import com.likhith.contractforge.config.OpenAiProperties;
import com.likhith.contractforge.exception.LlmNotConfiguredException;
import com.likhith.contractforge.exception.SnapshotNotFoundException;
import com.likhith.contractforge.exception.SnapshotsNotAvailableException;
import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.LlmScenarioBatch;
import com.likhith.contractforge.model.ScenarioArtifact;
import com.likhith.contractforge.model.ScenarioGenerationRequest;
import com.likhith.contractforge.model.ScenarioGenerationResponse;
import com.likhith.contractforge.model.ScenarioValidationResult;

import lombok.RequiredArgsConstructor;

/**
 * Orchestrates one scenario-generation request: load the trusted parser
 * snapshot for the requested endpoint, ask the LLM for candidate scenarios,
 * validate every one deterministically, and return only the outcome - never
 * the model's raw, unvalidated output.
 */
@Service
@RequiredArgsConstructor
public class ScenarioGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioGenerationService.class);
    private static final int MIN_SCENARIOS = 1;
    private static final int MAX_SCENARIOS = 8;

    private final ContractSnapshotCache cache;
    private final OpenAiProperties openAiProperties;
    private final LlmScenarioClient llmScenarioClient;
    private final ScenarioValidator scenarioValidator;
    private final ScenarioArtifactStore scenarioArtifactStore;

    public ScenarioGenerationResponse generate(ScenarioGenerationRequest request) {
        EndpointSnapshot endpoint = resolveEndpoint(request.endpointId());

        if (!openAiProperties.hasApiKey()) {
            throw new LlmNotConfiguredException(
                    "OPENAI_API_KEY is not configured; scenario generation is unavailable");
        }

        int effectiveMax = clamp(request.maxScenarios() != null ? request.maxScenarios() : openAiProperties.getMaxScenarios());

        long startedAt = System.currentTimeMillis();
        LlmScenarioBatch batch = llmScenarioClient.generateScenarios(endpoint, request.intent(), effectiveMax);
        long durationMs = System.currentTimeMillis() - startedAt;

        ScenarioValidationResult validation = scenarioValidator.validate(endpoint, batch);

        log.info("Scenario generation for {} using model '{}' took {} ms: requested={}, accepted={}, rejected={}",
                endpoint.endpointId(), openAiProperties.getModel(), durationMs, effectiveMax,
                validation.accepted().size(), validation.rejected().size());

        // Only ever the accepted, validated scenarios are persisted - rejected scenarios
        // never leave this method.
        ScenarioArtifact artifact = new ScenarioArtifact(
                UUID.randomUUID(), Instant.now(), endpoint.endpointId(), endpoint.method(), endpoint.path(),
                validation.accepted());
        Path artifactPath = scenarioArtifactStore.save(artifact);

        return new ScenarioGenerationResponse(
                endpoint.endpointId(),
                openAiProperties.getModel(),
                validation.accepted(),
                validation.rejected(),
                validation.accepted().size(),
                validation.rejected().size(),
                Instant.now(),
                artifact.artifactId(),
                artifactPath.toString());
    }

    private EndpointSnapshot resolveEndpoint(String endpointId) {
        ContractParseResult result = cache.get()
                .orElseThrow(() -> new SnapshotsNotAvailableException(
                        "No contract has been parsed yet. Call POST /api/contracts/parse first."));

        return result.endpoints().stream()
                .filter(e -> e.endpointId().equals(endpointId))
                .findFirst()
                .orElseThrow(() -> new SnapshotNotFoundException(
                        "No snapshot found for endpoint id '" + endpointId + "'"));
    }

    private int clamp(int requested) {
        return Math.max(MIN_SCENARIOS, Math.min(MAX_SCENARIOS, requested));
    }
}
