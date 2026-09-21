package com.likhith.contractforge.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Documents the safety boundaries for executing scenarios against a real
 * HTTP target: only these base URLs are ever allowed, with these timeouts.
 * Bound here for a single source of truth in application.yml. The Phase 3
 * test harness itself ({@code executor.ScenarioHttpExecutor}) runs as a plain
 * JUnit test with no Spring context, so it re-reads the same property names
 * as JVM system properties with matching defaults, rather than injecting this
 * class directly.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "contractforge.execution")
public class ExecutionProperties {

    private List<String> allowedBaseUrls = List.of("http://localhost:8080", "http://127.0.0.1:8080");
    private long connectTimeoutMs = 5000;
    private long readTimeoutMs = 15000;
}
