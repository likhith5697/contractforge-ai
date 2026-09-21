package com.likhith.contractforge.exception;

/**
 * The scenario artifact could not be written to or read from local disk.
 * Deliberately left unmapped in GlobalExceptionHandler - it falls through to
 * the generic 500 handler, which is correct: a local filesystem failure on
 * ContractForge's own machine is an internal server error, not something the
 * caller did wrong.
 */
public class ScenarioArtifactPersistenceException extends RuntimeException {

    public ScenarioArtifactPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
