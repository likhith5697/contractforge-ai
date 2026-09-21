package com.likhith.contractforge.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.likhith.contractforge.model.ApiScenario;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.FieldSnapshot;
import com.likhith.contractforge.model.LlmScenarioBatch;
import com.likhith.contractforge.model.ScenarioCategory;
import com.likhith.contractforge.model.ScenarioRejection;
import com.likhith.contractforge.model.ScenarioValidationResult;

/**
 * The actual trust boundary for LLM output. Every rule here is deterministic
 * and re-derivable from the endpoint's own {@link EndpointSnapshot} - nothing
 * about "is this a good test" is judged, only "does this payload match the
 * contract it claims to test."
 *
 * <p>Per-category rules:
 * <ul>
 *   <li>HAPPY_PATH: every required field present, every present field fully
 *       valid, no {@code violatedRulePath}, expectedStatus is a documented
 *       success code.</li>
 *   <li>BUSINESS_RISK: same bar as HAPPY_PATH - a business-risk scenario is
 *       still meant to be a fully schema-valid call (the risk is in the
 *       business meaning of the values, not a broken field), so {@code
 *       violatedRulePath} must be null here too.</li>
 *   <li>NEGATIVE: {@code violatedRulePath} required and must reference a known
 *       field; every other field is held to the HAPPY_PATH bar; the flagged
 *       field itself must be demonstrably invalid (missing-while-required, or
 *       present but failing a type/enum/pattern/range/format check) - a
 *       "negative" scenario that doesn't actually break anything is rejected.
 *       expectedStatus is a documented error code.</li>
 *   <li>BOUNDARY: same shape as NEGATIVE, except the flagged field is only
 *       required to type-match if present - a boundary case can legitimately
 *       sit exactly at an allowed edge.</li>
 * </ul>
 */
@Component
public class ScenarioValidator {

    private static final Logger log = LoggerFactory.getLogger(ScenarioValidator.class);
    private static final int MIN_SCENARIOS = 1;
    private static final int MAX_SCENARIOS = 8;

    public ScenarioValidationResult validate(EndpointSnapshot endpoint, LlmScenarioBatch batch) {
        if (batch == null || !endpointIdMatches(batch.endpointId(), endpoint.endpointId())) {
            String got = batch == null ? "null" : String.valueOf(batch.endpointId());
            return wholeBatchRejected("Response endpointId '" + got + "' does not match requested endpoint '"
                    + endpoint.endpointId() + "'");
        }

        List<ApiScenario> scenarios = batch.scenarios() == null ? List.of() : batch.scenarios();
        if (scenarios.size() < MIN_SCENARIOS || scenarios.size() > MAX_SCENARIOS) {
            return wholeBatchRejected("Expected between " + MIN_SCENARIOS + " and " + MAX_SCENARIOS
                    + " scenarios, got " + scenarios.size());
        }

        Set<String> seenNames = new HashSet<>();
        for (ApiScenario scenario : scenarios) {
            String name = scenario.name() == null ? "" : scenario.name();
            if (!seenNames.add(name)) {
                return wholeBatchRejected("Scenario name '" + name + "' is not unique");
            }
        }

        Map<String, FieldSnapshot> fieldsByPath = new LinkedHashMap<>();
        for (FieldSnapshot field : endpoint.requestFields()) {
            fieldsByPath.put(field.jsonPath(), field);
        }

        List<ApiScenario> accepted = new ArrayList<>();
        List<ScenarioRejection> rejected = new ArrayList<>();
        for (ApiScenario scenario : scenarios) {
            List<String> reasons = validateScenario(endpoint, fieldsByPath, scenario);
            if (reasons.isEmpty()) {
                accepted.add(scenario);
            } else {
                rejected.add(new ScenarioRejection(scenario.name(), reasons));
            }
        }

        return new ScenarioValidationResult(accepted, rejected);
    }

    private List<String> validateScenario(EndpointSnapshot endpoint, Map<String, FieldSnapshot> fieldsByPath,
            ApiScenario scenario) {
        List<String> reasons = new ArrayList<>();

        if (scenario.name() == null || scenario.name().isBlank()) {
            reasons.add("name is required");
        }
        if (scenario.category() == null) {
            reasons.add("category is required");
            return reasons; // nothing further can be checked without knowing the category
        }
        if (scenario.payload() == null || !scenario.payload().isObject()) {
            reasons.add("payload must be a JSON object");
            return reasons;
        }

        Map<String, JsonNode> payloadByPath = new LinkedHashMap<>();
        flattenPayload("", scenario.payload(), payloadByPath);

        for (String path : payloadByPath.keySet()) {
            if (!fieldsByPath.containsKey(path)) {
                reasons.add("Unknown field '" + path + "' is not present in the endpoint schema");
            }
        }

        boolean isNegativeOrBoundary = scenario.category() == ScenarioCategory.NEGATIVE
                || scenario.category() == ScenarioCategory.BOUNDARY;

        if (isNegativeOrBoundary) {
            validateViolatedRulePath(scenario, fieldsByPath, reasons);
        } else if (scenario.violatedRulePath() != null) {
            reasons.add(scenario.category() + " scenario must not set violatedRulePath");
        }

        String violatedPath = isNegativeOrBoundary ? scenario.violatedRulePath() : null;
        boolean flaggedFieldWasInvalid = false;

        for (FieldSnapshot field : fieldsByPath.values()) {
            boolean isFlaggedField = field.jsonPath().equals(violatedPath);
            JsonNode value = payloadByPath.get(field.jsonPath());
            boolean present = value != null && !value.isNull();

            if (isFlaggedField && scenario.category() == ScenarioCategory.BOUNDARY) {
                // A boundary case may legitimately sit exactly at an allowed edge (valid) or just
                // past it (invalid) - only require it to be present-and-type-correct-if-present,
                // not necessarily invalid.
                if (present && !typeMatches(field, value)) {
                    reasons.add("Field '" + field.jsonPath() + "' does not match expected type " + field.type());
                } else if (!present && field.required()) {
                    flaggedFieldWasInvalid = true; // omitting a required field is itself a valid boundary break
                }
                continue;
            }

            if (!present) {
                if (field.required() && !isFlaggedField) {
                    reasons.add("Missing required field '" + field.jsonPath() + "'");
                } else if (field.required() && isFlaggedField) {
                    flaggedFieldWasInvalid = true;
                }
                continue;
            }

            List<String> fieldReasons = validateFieldValue(field, value);
            if (isFlaggedField) {
                flaggedFieldWasInvalid = !fieldReasons.isEmpty();
                // The flagged field's own violation is expected and not itself a rejection reason.
            } else {
                reasons.addAll(fieldReasons);
            }
        }

        if (scenario.category() == ScenarioCategory.NEGATIVE && violatedPath != null && !flaggedFieldWasInvalid) {
            reasons.add("violatedRulePath '" + violatedPath + "' does not actually violate any detectable rule");
        }

        validateExpectedStatus(endpoint, scenario, reasons);

        return reasons;
    }

    private void validateViolatedRulePath(ApiScenario scenario, Map<String, FieldSnapshot> fieldsByPath,
            List<String> reasons) {
        String violatedRulePath = scenario.violatedRulePath();
        if (violatedRulePath == null || violatedRulePath.isBlank()) {
            reasons.add(scenario.category() + " scenario must set violatedRulePath to a real field path");
        } else if (!fieldsByPath.containsKey(violatedRulePath)) {
            reasons.add("violatedRulePath '" + violatedRulePath + "' is not a known field path");
        }
    }

    private void validateExpectedStatus(EndpointSnapshot endpoint, ApiScenario scenario, List<String> reasons) {
        boolean isSuccessCategory = scenario.category() == ScenarioCategory.HAPPY_PATH
                || scenario.category() == ScenarioCategory.BUSINESS_RISK;
        List<Integer> documented = isSuccessCategory
                ? endpoint.successStatusCodes()
                : endpoint.documentedErrorStatusCodes();

        if (!documented.contains(scenario.expectedStatus())) {
            reasons.add("expectedStatus " + scenario.expectedStatus() + " is not a documented "
                    + (isSuccessCategory ? "success" : "error") + " status code " + documented
                    + " for this operation");
        }
    }

    /** Full constraint check used for every field that isn't the scenario's declared violation. */
    private List<String> validateFieldValue(FieldSnapshot field, JsonNode value) {
        List<String> reasons = new ArrayList<>();

        if (!typeMatches(field, value)) {
            reasons.add("Field '" + field.jsonPath() + "' expected type " + field.type() + " but got "
                    + value.getNodeType());
            return reasons; // further checks assume the type already matches
        }

        if (!field.enumValues().isEmpty() && !field.enumValues().contains(value.asText())) {
            reasons.add("Field '" + field.jsonPath() + "' value '" + value.asText()
                    + "' is not one of the allowed values " + field.enumValues());
        }

        if (field.pattern() != null && value.isTextual()) {
            if (!safePatternMatches(field.pattern(), value.asText())) {
                reasons.add("Field '" + field.jsonPath() + "' value does not match pattern " + field.pattern());
            }
        }

        if (value.isTextual()) {
            int length = value.asText().length();
            if (field.minLength() != null && length < field.minLength()) {
                reasons.add("Field '" + field.jsonPath() + "' is shorter than minLength " + field.minLength());
            }
            if (field.maxLength() != null && length > field.maxLength()) {
                reasons.add("Field '" + field.jsonPath() + "' is longer than maxLength " + field.maxLength());
            }
        }

        if (value.isNumber()) {
            BigDecimal decimal = value.decimalValue();
            if (field.minimum() != null && decimal.compareTo(field.minimum()) < 0) {
                reasons.add("Field '" + field.jsonPath() + "' value " + decimal + " is below minimum " + field.minimum());
            }
            if (field.maximum() != null && decimal.compareTo(field.maximum()) > 0) {
                reasons.add("Field '" + field.jsonPath() + "' value " + decimal + " is above maximum " + field.maximum());
            }
        }

        if (field.format() != null && value.isTextual() && !formatMatches(field.format(), value.asText())) {
            reasons.add("Field '" + field.jsonPath() + "' value does not match format '" + field.format() + "'");
        }

        return reasons;
    }

    private boolean typeMatches(FieldSnapshot field, JsonNode value) {
        return switch (field.type()) {
            case "string" -> value.isTextual();
            case "integer" -> value.isIntegralNumber();
            case "number" -> value.isNumber();
            case "boolean" -> value.isBoolean();
            case "array" -> value.isArray();
            case "object" -> value.isObject();
            default -> true; // an unrecognized schema type shouldn't block validation of everything else
        };
    }

    private boolean safePatternMatches(String pattern, String value) {
        try {
            return Pattern.compile(pattern).matcher(value).matches();
        } catch (PatternSyntaxException ex) {
            log.warn("Field has an unparseable regex pattern '{}'; skipping pattern check", pattern);
            return true;
        }
    }

    /** Only the formats actually present in banking-api schemas are checked; anything else is passed through. */
    private boolean formatMatches(String format, String value) {
        try {
            switch (format) {
                case "date" -> LocalDate.parse(value);
                case "date-time" -> OffsetDateTime.parse(value);
                default -> {
                    return true;
                }
            }
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    /**
     * Mirrors SchemaFlattener's path convention over an actual JSON instance
     * rather than a schema: objects flatten to dot paths, and an array is
     * recorded both as its own leaf (so "unknown field" checks work on the
     * array field itself) and recursed into under "path[]" for its items.
     */
    private void flattenPayload(String prefix, JsonNode node, Map<String, JsonNode> out) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String childPath = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenPayload(childPath, entry.getValue(), out);
            });
        } else if (node.isArray()) {
            out.put(prefix, node);
            for (JsonNode item : node) {
                flattenPayload(prefix + "[]", item, out);
            }
        } else {
            out.put(prefix, node);
        }
    }

    /**
     * Some models echo the endpointId back without the "METHOD:" prefix we gave
     * them (e.g. "/api/payments/scheduled" instead of "POST:/api/payments/scheduled")
     * despite the prompt supplying it verbatim - this tolerates that specific
     * formatting drift while still catching genuine drift to a different path.
     */
    private boolean endpointIdMatches(String returned, String expected) {
        if (returned == null || returned.isBlank()) {
            return false;
        }
        String normalized = returned.contains(":") ? returned : "POST:" + returned;
        return normalized.equals(expected);
    }

    private ScenarioValidationResult wholeBatchRejected(String reason) {
        log.warn("Rejecting entire LLM response: {}", reason);
        return new ScenarioValidationResult(List.of(), List.of(new ScenarioRejection("<batch>", List.of(reason))));
    }
}
