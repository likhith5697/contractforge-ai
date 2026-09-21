package com.likhith.contractforge.exception;

/**
 * The OpenAPI source was unreachable, timed out, or responded with a non-2xx
 * status. Always mapped to HTTP 502 (Bad Gateway) - the caller's request was
 * fine, it's the upstream banking-api that failed.
 */
public class OpenApiFetchException extends RuntimeException {

    public OpenApiFetchException(String message) {
        super(message);
    }

    public OpenApiFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
