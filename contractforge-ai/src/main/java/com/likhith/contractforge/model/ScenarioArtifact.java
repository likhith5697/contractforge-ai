package com.likhith.contractforge.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The durable, persisted record of one successful scenario-generation run -
 * only ever contains {@code acceptedScenarios} (validated, trusted). Rejected
 * scenarios never leave the generation response; they are never written here.
 *
 * <p>{@code targetMethod}/{@code targetPath} are copied from the trusted
 * {@code EndpointSnapshot} at generation time, not from anything the LLM
 * returned, so the Phase 3 execution harness has an unambiguous, tamper-proof
 * record of exactly where each scenario is meant to be sent.
 */
public record ScenarioArtifact(
        UUID artifactId,
        Instant createdAt,
        String endpointId,
        String targetMethod,
        String targetPath,
        List<ApiScenario> acceptedScenarios) {
}
