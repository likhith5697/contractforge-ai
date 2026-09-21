package com.likhith.contractforge.model;

import java.util.List;

/**
 * Why one scenario (or, for batch-level problems, the whole response) was
 * rejected. {@code name} is {@code "<batch>"} for a failure that isn't
 * attributable to a single scenario (e.g. the response's endpointId didn't
 * match the request, or the scenario count/name-uniqueness rules were broken).
 */
public record ScenarioRejection(
        String name,
        List<String> reasons) {
}
