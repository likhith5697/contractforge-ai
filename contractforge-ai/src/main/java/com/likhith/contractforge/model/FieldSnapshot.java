package com.likhith.contractforge.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * One flattened leaf field from a request schema, addressed by its full JSON
 * path (e.g. "scheduleDetails.frequency" or "beneficiaries[].bankDetails.routingNumber").
 * Only leaf (scalar or array) fields get a snapshot - pure object containers are
 * walked but never emitted themselves, since a downstream scenario generator only
 * needs to know what value to place at each addressable path.
 *
 * {@code example} is stringified regardless of its original JSON type so the
 * record stays simple and its equality/serialization stays predictable.
 */
public record FieldSnapshot(
        String jsonPath,
        String fieldName,
        String type,
        String format,
        boolean required,
        boolean nullable,
        String description,
        String example,
        List<String> enumValues,
        BigDecimal minimum,
        BigDecimal maximum,
        Integer minLength,
        Integer maxLength,
        String pattern,
        boolean isArray,
        String arrayItemType) {
}
