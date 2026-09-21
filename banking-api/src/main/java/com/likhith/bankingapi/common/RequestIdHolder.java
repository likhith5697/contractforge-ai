package com.likhith.bankingapi.common;

import org.slf4j.MDC;

/**
 * Reads the current request's correlation id, populated by the RequestIdFilter
 * for every incoming HTTP request via the X-Request-Id header (or a generated UUID).
 */
public final class RequestIdHolder {

    public static final String MDC_KEY = "requestId";

    private RequestIdHolder() {
    }

    public static String get() {
        String requestId = MDC.get(MDC_KEY);
        return requestId != null ? requestId : "UNKNOWN";
    }
}
