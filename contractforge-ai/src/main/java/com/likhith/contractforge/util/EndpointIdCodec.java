package com.likhith.contractforge.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Endpoint ids look like "POST:/api/payments/scheduled" - unsafe to place
 * directly in a URL path segment (":" and "/" both have special meaning).
 * Base64 URL-safe encoding gives a round-trippable, path-segment-safe token
 * without inventing an ad hoc escaping scheme.
 */
public final class EndpointIdCodec {

    private EndpointIdCodec() {
    }

    public static String encode(String endpointId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(endpointId.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encodedEndpointId) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedEndpointId);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("'" + encodedEndpointId + "' is not a valid encoded endpoint id", ex);
        }
    }
}
