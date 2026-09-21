package com.likhith.contractforge.executor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likhith.contractforge.executor.model.ScenarioExecutionResult;
import com.likhith.contractforge.executor.model.TestRunReport;
import com.likhith.contractforge.model.ScenarioArtifact;

/**
 * Fixed, generic JUnit 5 Dynamic Test harness: it never contains any banking
 * endpoint or field name. Given a scenario artifact (produced by Phase 2) and
 * a base URL, it turns every accepted scenario into one dynamic test that
 * makes a real HTTP call and asserts the actual status matches what was
 * validated as expected.
 *
 * <p>Reads two system properties, both optional so a plain {@code mvn test}
 * stays deterministic and offline:
 * <ul>
 *   <li>{@code contractforge.scenario-artifact} - path to the artifact JSON.
 *       Absent -&gt; this factory returns an empty stream (no tests, no
 *       network calls, nothing skipped-and-red).</li>
 *   <li>{@code contractforge.test-base-url} - defaults to
 *       {@code http://localhost:8080} (the local banking-api test app).</li>
 * </ul>
 */
public class ContractForgeDynamicApiTest {

    private static final Logger log = LoggerFactory.getLogger(ContractForgeDynamicApiTest.class);
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    @TestFactory
    Stream<DynamicTest> executeAcceptedScenarios() {
        String artifactPathProperty = System.getProperty("contractforge.scenario-artifact");
        if (artifactPathProperty == null || artifactPathProperty.isBlank()) {
            log.info("contractforge.scenario-artifact is not set; skipping scenario execution");
            return Stream.empty();
        }

        ScenarioArtifact artifact = loadArtifact(Path.of(artifactPathProperty));
        if (artifact.acceptedScenarios().isEmpty()) {
            log.info("Scenario artifact {} has no accepted scenarios; nothing to execute", artifact.artifactId());
            return Stream.empty();
        }

        String baseUrl = System.getProperty("contractforge.test-base-url", DEFAULT_BASE_URL);
        ScenarioHttpExecutor executor = new ScenarioHttpExecutor(baseUrl);
        ExecutionReportWriter reportWriter = new ExecutionReportWriter();

        List<ScenarioExecutionResult> results = Collections.synchronizedList(new ArrayList<>());
        Instant startedAt = Instant.now();

        log.info("Executing {} accepted scenario(s) for {} against {}",
                artifact.acceptedScenarios().size(), artifact.endpointId(), baseUrl);

        return artifact.acceptedScenarios().stream()
                .map(scenario -> DynamicTest.dynamicTest(scenario.name(), () ->
                        executor.executeAndAssert(artifact, scenario, result -> {
                            results.add(result);
                            reportWriter.writeSafely(buildReport(artifact, baseUrl, startedAt, results));
                        })));
    }

    private TestRunReport buildReport(ScenarioArtifact artifact, String baseUrl, Instant startedAt,
            List<ScenarioExecutionResult> resultsSoFar) {
        List<ScenarioExecutionResult> snapshot;
        synchronized (resultsSoFar) {
            snapshot = List.copyOf(resultsSoFar);
        }
        int passed = (int) snapshot.stream().filter(ScenarioExecutionResult::passed).count();
        return new TestRunReport(artifact.artifactId(), artifact.endpointId(), baseUrl, startedAt, Instant.now(),
                snapshot.size(), passed, snapshot.size() - passed, snapshot);
    }

    private ScenarioArtifact loadArtifact(Path path) {
        try {
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            return mapper.readValue(path.toFile(), ScenarioArtifact.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read scenario artifact at " + path, ex);
        }
    }
}
