package com.likhith.contractforge.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.likhith.contractforge.config.OpenApiSourceProperties;
import com.likhith.contractforge.exception.OpenApiParseException;
import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.parser.OpenApiContractParser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;

/**
 * Orchestrates one end-to-end parse run: fetch the configured OpenAPI source,
 * parse it with swagger-parser, extract POST endpoint snapshots, and cache the
 * result. This is the only place the configured source URL is read from.
 */
@Service
@RequiredArgsConstructor
public class ContractParseService {

    private static final Logger log = LoggerFactory.getLogger(ContractParseService.class);

    private final OpenApiSourceProperties properties;
    private final OpenApiFetcher openApiFetcher;
    private final OpenApiContractParser openApiContractParser;
    private final ContractSnapshotCache cache;

    public ContractParseResult parseConfiguredSource() {
        String sourceUrl = properties.getSourceUrl();
        String rawContract = openApiFetcher.fetchRawContract(sourceUrl);

        OpenAPI openApi = parse(sourceUrl, rawContract);

        List<EndpointSnapshot> endpoints = openApiContractParser.extractPostEndpoints(openApi);
        ContractParseResult result = new ContractParseResult(sourceUrl, Instant.now(), endpoints.size(), endpoints);
        cache.store(result);

        log.info("Parsed OpenAPI contract from {}: {} POST endpoint(s) discovered", sourceUrl, endpoints.size());
        return result;
    }

    private OpenAPI parse(String sourceUrl, String rawContract) {
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(rawContract, null, parseOptions());

        OpenAPI openApi = parseResult.getOpenAPI();
        if (openApi == null || openApi.getPaths() == null) {
            String issues = (parseResult.getMessages() == null || parseResult.getMessages().isEmpty())
                    ? "document is not a recognizable OpenAPI 3 contract"
                    : String.join("; ", parseResult.getMessages());
            throw new OpenApiParseException("The document at " + sourceUrl + " could not be parsed: " + issues);
        }

        if (parseResult.getMessages() != null && !parseResult.getMessages().isEmpty()) {
            log.warn("swagger-parser reported {} non-fatal issue(s) while parsing {}: {}",
                    parseResult.getMessages().size(), sourceUrl, parseResult.getMessages());
        }

        return openApi;
    }

    private ParseOptions parseOptions() {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        // Deliberately NOT resolveFully: we walk $ref chains ourselves in SchemaFlattener
        // so we control cycle detection instead of relying on the parser's own inliner.
        options.setResolveFully(false);
        return options;
    }
}
