package com.likhith.contractforge.model;

import java.util.List;

/**
 * A compact, deterministic description of a single POST operation discovered
 * in the source OpenAPI document, including its flattened request schema.
 *
 * <p>{@code successStatusCodes} and {@code documentedErrorStatusCodes} are read
 * directly from the operation's {@code responses} map - never assumed. Many
 * real endpoints only document a generic 200 even when they actually return
 * 201 at runtime (springdoc's default when no {@code @ApiResponse} is present),
 * and some document no error codes at all. Downstream consumers (e.g. scenario
 * generation) must treat an empty list as "nothing documented", not "anything
 * goes".
 */
public record EndpointSnapshot(
        String endpointId,
        String method,
        String path,
        String operationId,
        String summary,
        List<String> tags,
        List<ApiHeaderSnapshot> requiredHeaders,
        String requestSchemaName,
        List<FieldSnapshot> requestFields,
        List<Integer> successStatusCodes,
        List<Integer> documentedErrorStatusCodes) {
}
