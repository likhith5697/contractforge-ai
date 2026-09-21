package com.likhith.contractforge.service;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import com.likhith.contractforge.model.ScenarioArtifact;

/**
 * Persists and retrieves scenario artifacts. Kept as an interface - with
 * {@link FileSystemScenarioArtifactStore} as the only production
 * implementation - so it can be swapped for an in-memory/temp-dir variant in
 * tests without touching {@code ScenarioGenerationService}.
 */
public interface ScenarioArtifactStore {

    /**
     * @return the path the artifact was written to
     */
    Path save(ScenarioArtifact artifact);

    Optional<ScenarioArtifact> findById(UUID artifactId);
}
