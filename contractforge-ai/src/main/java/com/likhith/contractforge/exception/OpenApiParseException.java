package com.likhith.contractforge.exception;

/**
 * The fetched document could not be parsed as a valid OpenAPI contract.
 * Always mapped to HTTP 422 (Unprocessable Entity) - the request succeeded,
 * but the content it returned isn't usable.
 */
public class OpenApiParseException extends RuntimeException {

    public OpenApiParseException(String message) {
        super(message);
    }
}
