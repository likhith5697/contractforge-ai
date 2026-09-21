package com.likhith.contractforge.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.likhith.contractforge.model.ApiHeaderSnapshot;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.FieldSnapshot;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Walks a parsed {@link OpenAPI} document, keeps only POST operations, and
 * turns each into a compact {@link EndpointSnapshot}. Endpoints without an
 * {@code application/json} request body are skipped (with the reason logged)
 * rather than failing the whole parse run.
 */
@Component
@RequiredArgsConstructor
public class OpenApiContractParser {

    private static final Logger log = LoggerFactory.getLogger(OpenApiContractParser.class);

    private final SchemaFlattener schemaFlattener;

    public List<EndpointSnapshot> extractPostEndpoints(OpenAPI openApi) {
        List<EndpointSnapshot> endpoints = new ArrayList<>();
        if (openApi.getPaths() == null) {
            return endpoints;
        }

        for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
            Operation postOperation = pathEntry.getValue().getPost();
            if (postOperation == null) {
                continue;
            }
            endpoints.add(toSnapshot(pathEntry.getKey(), postOperation, openApi));
        }

        endpoints.sort(Comparator.comparing(EndpointSnapshot::endpointId));
        return endpoints;
    }

    private EndpointSnapshot toSnapshot(String path, Operation operation, OpenAPI openApi) {
        String endpointId = "POST:" + path;

        RequestBodySchema requestBodySchema = resolveJsonRequestBodySchema(endpointId, operation);
        List<FieldSnapshot> requestFields = requestBodySchema == null
                ? List.of()
                : schemaFlattener.flatten(requestBodySchema.schema(), openApi);

        if (requestBodySchema != null) {
            log.info("Parsed {}: {} field(s) extracted from schema '{}'",
                    endpointId, requestFields.size(), requestBodySchema.schemaName());
        }

        ResponseStatusCodes statusCodes = extractResponseStatusCodes(operation);

        return new EndpointSnapshot(
                endpointId,
                "POST",
                path,
                operation.getOperationId(),
                operation.getSummary(),
                operation.getTags() == null ? List.of() : List.copyOf(operation.getTags()),
                extractRequiredHeaders(operation),
                requestBodySchema == null ? null : requestBodySchema.schemaName(),
                requestFields,
                statusCodes.successCodes(),
                statusCodes.errorCodes());
    }

    /**
     * Reads the operation's declared {@code responses} map at face value - never
     * assumes a 201 for a create endpoint or a 400 for validation. A "default"
     * or otherwise non-numeric response key isn't a specific status code, so it
     * is ignored rather than guessed at.
     */
    private ResponseStatusCodes extractResponseStatusCodes(Operation operation) {
        List<Integer> success = new ArrayList<>();
        List<Integer> errors = new ArrayList<>();

        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            for (String code : responses.keySet()) {
                try {
                    int statusCode = Integer.parseInt(code);
                    if (statusCode >= 200 && statusCode < 300) {
                        success.add(statusCode);
                    } else if (statusCode >= 400) {
                        errors.add(statusCode);
                    }
                } catch (NumberFormatException ex) {
                    log.debug("Ignoring non-numeric response key '{}' on operation '{}'", code,
                            operation.getOperationId());
                }
            }
        }

        Collections.sort(success);
        Collections.sort(errors);
        return new ResponseStatusCodes(success, errors);
    }

    private List<ApiHeaderSnapshot> extractRequiredHeaders(Operation operation) {
        if (operation.getParameters() == null) {
            return List.of();
        }
        return operation.getParameters().stream()
                .filter(parameter -> "header".equals(parameter.getIn()))
                .filter(parameter -> Boolean.TRUE.equals(parameter.getRequired()))
                .map(parameter -> new ApiHeaderSnapshot(
                        parameter.getName(),
                        true,
                        parameter.getSchema() != null ? parameter.getSchema().getType() : null,
                        parameter.getDescription()))
                .toList();
    }

    private RequestBodySchema resolveJsonRequestBodySchema(String endpointId, Operation operation) {
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody == null || requestBody.getContent() == null) {
            log.info("Skipping {}: no request body declared", endpointId);
            return null;
        }

        MediaType jsonMedia = requestBody.getContent().get("application/json");
        if (jsonMedia == null || jsonMedia.getSchema() == null) {
            log.info("Skipping {}: no application/json request body content", endpointId);
            return null;
        }

        Schema<?> schema = jsonMedia.getSchema();
        String schemaName = schema.get$ref() != null ? simpleRefName(schema.get$ref()) : null;
        return new RequestBodySchema(schemaName, schema);
    }

    private String simpleRefName(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }

    private record RequestBodySchema(String schemaName, Schema<?> schema) {
    }

    private record ResponseStatusCodes(List<Integer> successCodes, List<Integer> errorCodes) {
    }
}
