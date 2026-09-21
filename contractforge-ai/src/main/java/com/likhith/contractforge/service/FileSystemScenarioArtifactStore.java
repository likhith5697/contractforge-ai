package com.likhith.contractforge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likhith.contractforge.config.ArtifactProperties;
import com.likhith.contractforge.exception.ScenarioArtifactPersistenceException;
import com.likhith.contractforge.model.ScenarioArtifact;

import lombok.RequiredArgsConstructor;

/**
 * Writes each artifact as one pretty-printed JSON file named
 * {@code <artifactId>.json} under the configured scenario directory - simple,
 * human-inspectable, and directly readable by the Phase 3 harness without any
 * shared database or service call.
 */
@Component
@RequiredArgsConstructor
public class FileSystemScenarioArtifactStore implements ScenarioArtifactStore {

    private static final Logger log = LoggerFactory.getLogger(FileSystemScenarioArtifactStore.class);

    private final ArtifactProperties artifactProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Path save(ScenarioArtifact artifact) {
        Path directory = Path.of(artifactProperties.getScenarioDirectory());
        Path file = directory.resolve(artifact.artifactId() + ".json");
        try {
            Files.createDirectories(directory);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), artifact);
        } catch (IOException ex) {
            throw new ScenarioArtifactPersistenceException(
                    "Failed to persist scenario artifact " + artifact.artifactId(), ex);
        }
        log.info("Saved scenario artifact {} ({} accepted scenario(s)) to {}",
                artifact.artifactId(), artifact.acceptedScenarios().size(), file);
        return file;
    }

    @Override
    public Optional<ScenarioArtifact> findById(UUID artifactId) {
        Path file = Path.of(artifactProperties.getScenarioDirectory()).resolve(artifactId + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(file.toFile(), ScenarioArtifact.class));
        } catch (IOException ex) {
            throw new ScenarioArtifactPersistenceException("Failed to read scenario artifact " + artifactId, ex);
        }
    }
}
