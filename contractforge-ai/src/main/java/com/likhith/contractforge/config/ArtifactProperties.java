package com.likhith.contractforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Where ContractForge writes its runtime artifacts. {@code scenarioDirectory}
 * is read by {@code FileSystemScenarioArtifactStore} inside the running app.
 * {@code reportDirectory} is documented here for a single source of truth in
 * application.yml, but is actually consumed by the Phase 3 test harness
 * ({@code executor.ExecutionReportWriter}), which runs as a plain JUnit test
 * with no Spring context - it reads the same property name as a JVM system
 * property instead, with the identical default.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "contractforge.artifacts")
public class ArtifactProperties {

    private String scenarioDirectory = "target/contractforge/scenarios";
    private String reportDirectory = "target/contractforge/reports";
}
