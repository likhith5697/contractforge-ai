package com.likhith.contractforge.exception;

/**
 * Raised when scenario generation is requested but OPENAI_API_KEY is absent.
 * Always mapped to HTTP 503 - no call to OpenAI is ever attempted in this case.
 */
public class LlmNotConfiguredException extends RuntimeException {

    public LlmNotConfiguredException(String message) {
        super(message);
    }
}
