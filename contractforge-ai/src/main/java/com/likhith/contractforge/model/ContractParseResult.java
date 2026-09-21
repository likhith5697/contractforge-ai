package com.likhith.contractforge.model;

import java.time.Instant;
import java.util.List;

/**
 * The full result of one parse run, cached in memory and served back by the
 * snapshot endpoints until the next successful {@code POST /api/contracts/parse}.
 */
public record ContractParseResult(
        String sourceUrl,
        Instant parsedAt,
        int postEndpointCount,
        List<EndpointSnapshot> endpoints) {
}
