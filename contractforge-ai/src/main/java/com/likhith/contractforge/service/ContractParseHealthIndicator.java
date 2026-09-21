package com.likhith.contractforge.service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.likhith.contractforge.model.ContractParseResult;

import lombok.RequiredArgsConstructor;

/**
 * Reports UP once at least one contract has been successfully parsed in this
 * application's lifetime, and the source/endpoint count of the latest run.
 * Reports UNKNOWN (not DOWN - nothing is actually broken) beforehand, since
 * "never parsed yet" is a normal startup state, not a failure.
 * Exposed at /actuator/health under the "contractParse" component.
 */
@Component("contractParse")
@RequiredArgsConstructor
public class ContractParseHealthIndicator implements HealthIndicator {

    private final ContractSnapshotCache cache;

    @Override
    public Health health() {
        return cache.get()
                .map(this::upWithDetails)
                .orElseGet(() -> Health.unknown()
                        .withDetail("message", "No contract has been parsed yet; call POST /api/contracts/parse")
                        .build());
    }

    private Health upWithDetails(ContractParseResult result) {
        return Health.up()
                .withDetail("sourceUrl", result.sourceUrl())
                .withDetail("parsedAt", result.parsedAt().toString())
                .withDetail("postEndpointCount", result.postEndpointCount())
                .build();
    }
}
