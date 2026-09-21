package com.likhith.contractforge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.likhith.contractforge.exception.OpenApiFetchException;

import lombok.RequiredArgsConstructor;

/**
 * Fetches the raw OpenAPI document body over HTTP. Deliberately returns the
 * raw JSON string rather than deserializing here - parsing/validating the
 * OpenAPI structure itself is swagger-parser's job (see ContractParseService).
 */
@Component
@RequiredArgsConstructor
public class OpenApiFetcher {

    private static final Logger log = LoggerFactory.getLogger(OpenApiFetcher.class);

    private final RestClient contractForgeRestClient;

    public String fetchRawContract(String sourceUrl) {
        log.info("Fetching OpenAPI contract from {}", sourceUrl);
        try {
            String body = contractForgeRestClient.get()
                    .uri(sourceUrl)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                throw new OpenApiFetchException("OpenAPI source at " + sourceUrl + " returned an empty response body");
            }
            return body;
        } catch (RestClientResponseException ex) {
            throw new OpenApiFetchException(
                    "OpenAPI source at " + sourceUrl + " responded with HTTP " + ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new OpenApiFetchException(
                    "Could not reach OpenAPI source at " + sourceUrl + " (connection failed or timed out)", ex);
        }
    }
}
