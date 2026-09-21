package com.likhith.contractforge.ai;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likhith.contractforge.model.ApiHeaderSnapshot;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.FieldSnapshot;

/**
 * Builds the exact, minimal text sent to the LLM for one endpoint: never the
 * full OpenAPI document, never banking-api source - only the trusted parser
 * snapshot's fields, constraints, documented status codes, and the caller's
 * intent.
 */
@Component
public class ScenarioPromptFactory {

    public static final String SYSTEM_PROMPT = """
            You are a contract-aware API test-data planner.
            Return only the requested strict JSON object.
            Create synthetic data only. Never use real PII or real financial data.
            Every payload must use only fields present in the supplied schema.
            A happy-path payload must include every required field and satisfy all field constraints.
            A negative scenario may intentionally violate exactly one named rule; all unrelated fields must remain valid.
            Use complete nested JSON payloads, not partial patches.
            Do not invent URLs, headers, fields, schemas, auth tokens, response bodies, or status codes.
            Use only documented response status codes supplied in the input.
            Keep scenarios distinct and high value.
            """;

    public String buildUserPrompt(EndpointSnapshot endpoint, String intent, int maxScenarios) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Endpoint: ").append(endpoint.endpointId()).append('\n');
        prompt.append("Method: ").append(endpoint.method()).append('\n');
        prompt.append("Path: ").append(endpoint.path()).append('\n');
        if (endpoint.summary() != null && !endpoint.summary().isBlank()) {
            prompt.append("Summary: ").append(endpoint.summary()).append('\n');
        }
        prompt.append("Required headers: ").append(describeHeaders(endpoint.requiredHeaders())).append('\n');
        prompt.append("Documented success status codes: ").append(endpoint.successStatusCodes()).append('\n');
        prompt.append("Documented error status codes: ").append(endpoint.documentedErrorStatusCodes()).append('\n');
        prompt.append("Request schema name: ").append(endpoint.requestSchemaName()).append('\n');

        prompt.append("Fields (every payload path you may use, with its constraints):\n");
        for (FieldSnapshot field : endpoint.requestFields()) {
            prompt.append("  - ").append(describeField(field)).append('\n');
        }

        prompt.append("Caller intent: ")
                .append(intent == null || intent.isBlank() ? "General coverage across happy path, negative, boundary and business-risk cases" : intent)
                .append('\n');
        prompt.append("Maximum scenarios to generate: ").append(maxScenarios).append('\n');

        prompt.append("Rules for this response:\n");
        prompt.append("  - Use only the field paths listed above; do not invent any field.\n");
        prompt.append("  - A HAPPY_PATH or BUSINESS_RISK scenario's expectedStatus must be one of the documented success status codes above, and violatedRulePath must be null.\n");
        prompt.append("  - A NEGATIVE or BOUNDARY scenario's expectedStatus must be one of the documented error status codes above, and violatedRulePath must name exactly one field path from the list above.\n");
        prompt.append("  - If no error status codes are documented above, do not produce any NEGATIVE or BOUNDARY scenario.\n");
        prompt.append("  - Every scenario name must be unique.\n");

        return prompt.toString();
    }

    private String describeHeaders(List<ApiHeaderSnapshot> headers) {
        if (headers.isEmpty()) {
            return "none";
        }
        return headers.stream()
                .map(h -> h.name() + (h.type() != null ? " (" + h.type() + ")" : ""))
                .collect(Collectors.joining(", "));
    }

    private String describeField(FieldSnapshot field) {
        StringBuilder description = new StringBuilder();
        description.append(field.jsonPath()).append(": type=").append(field.type());
        if (field.format() != null) {
            description.append(", format=").append(field.format());
        }
        description.append(", required=").append(field.required());
        if (field.nullable()) {
            description.append(", nullable=true");
        }
        if (!field.enumValues().isEmpty()) {
            description.append(", enum=").append(field.enumValues());
        }
        if (field.minimum() != null) {
            description.append(", minimum=").append(field.minimum());
        }
        if (field.maximum() != null) {
            description.append(", maximum=").append(field.maximum());
        }
        if (field.minLength() != null) {
            description.append(", minLength=").append(field.minLength());
        }
        if (field.maxLength() != null) {
            description.append(", maxLength=").append(field.maxLength());
        }
        if (field.pattern() != null) {
            description.append(", pattern=").append(field.pattern());
        }
        if (field.isArray()) {
            description.append(", array of ").append(field.arrayItemType());
        }
        return description.toString();
    }
}
