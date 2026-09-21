package com.likhith.contractforge.exception;

/**
 * Raised when a requested artifact id has no corresponding saved artifact.
 * Always mapped to HTTP 404.
 */
public class ScenarioArtifactNotFoundException extends RuntimeException {

    public ScenarioArtifactNotFoundException(String message) {
        super(message);
    }
}
