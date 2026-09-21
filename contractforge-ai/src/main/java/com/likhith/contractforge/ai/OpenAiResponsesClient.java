package com.likhith.contractforge.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.likhith.contractforge.config.OpenAiProperties;
import com.likhith.contractforge.exception.LlmCommunicationException;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.LlmScenarioBatch;
import com.likhith.contractforge.model.ScenarioCategory;

import lombok.RequiredArgsConstructor;

/**
 * Talks to OpenAI's Responses API ({@code POST /v1/responses}) with structured
 * JSON-schema output, and parses the result into {@link LlmScenarioBatch}.
 *
 * <p>The JSON schema sent for structured output is deliberately non-strict:
 * the {@code payload} field is an endpoint-specific, dynamic JSON object, and
 * OpenAI's "strict" structured-output mode requires every object's properties
 * to be fully enumerated up front - which is exactly what {@code payload}
 * cannot be. Non-strict mode still instructs the model to conform to the
 * schema; the real enforcement is {@code ScenarioValidator}, which never
 * trusts this client's output regardless of what OpenAI promises.
 */
@Component
@RequiredArgsConstructor
public class OpenAiResponsesClient implements LlmScenarioClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiResponsesClient.class);
    private static final String RESPONSES_PATH = "/responses";

    private final RestClient openAiRestClient;
    private final OpenAiProperties openAiProperties;
    private final ScenarioPromptFactory scenarioPromptFactory;
    private final ObjectMapper objectMapper;

    @Override
    public LlmScenarioBatch generateScenarios(EndpointSnapshot endpoint, String intent, int maxScenarios) {
        String userPrompt = scenarioPromptFactory.buildUserPrompt(endpoint, intent, maxScenarios);
        ObjectNode requestBody = buildRequestBody(userPrompt);

        String rawResponse = callOpenAi(requestBody);
        String modelOutputText = extractOutputText(rawResponse);
        return parseScenarioBatch(modelOutputText);
    }

    private String callOpenAi(ObjectNode requestBody) {
        try {
            return openAiRestClient.post()
                    .uri(RESPONSES_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_AUTH_FAILED",
                    "OpenAI rejected the configured API key", ex);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw new LlmCommunicationException(HttpStatus.TOO_MANY_REQUESTS, "LLM_RATE_LIMITED",
                    "OpenAI rate limit exceeded; try again later", ex);
        } catch (HttpServerErrorException ex) {
            throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_UPSTREAM_ERROR",
                    "OpenAI returned a server error", ex);
        } catch (RestClientResponseException ex) {
            throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_UPSTREAM_ERROR",
                    "OpenAI request failed with HTTP " + ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new LlmCommunicationException(HttpStatus.GATEWAY_TIMEOUT, "LLM_TIMEOUT",
                    "Timed out waiting for OpenAI to respond", ex);
        }
    }

    private String extractOutputText(String rawResponse) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawResponse);
        } catch (JsonProcessingException ex) {
            throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_MALFORMED_RESPONSE",
                    "OpenAI response was not valid JSON", ex);
        }

        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }
                for (JsonNode contentItem : item.path("content")) {
                    String type = contentItem.path("type").asText();
                    if ("output_text".equals(type)) {
                        return contentItem.path("text").asText();
                    }
                    if ("refusal".equals(type)) {
                        throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_REFUSED",
                                "OpenAI declined to generate scenarios: " + contentItem.path("refusal").asText());
                    }
                }
            }
        }

        throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_EMPTY_RESPONSE",
                "OpenAI response did not contain any output text");
    }

    private LlmScenarioBatch parseScenarioBatch(String modelOutputText) {
        try {
            return objectMapper.readValue(modelOutputText, LlmScenarioBatch.class);
        } catch (JsonProcessingException ex) {
            throw new LlmCommunicationException(HttpStatus.BAD_GATEWAY, "LLM_MALFORMED_RESPONSE",
                    "OpenAI's structured output could not be parsed into the scenario contract", ex);
        }
    }

    private ObjectNode buildRequestBody(String userPrompt) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openAiProperties.getModel());

        ArrayNode input = root.putArray("input");
        input.addObject().put("role", "system").put("content", ScenarioPromptFactory.SYSTEM_PROMPT);
        input.addObject().put("role", "user").put("content", userPrompt);

        ObjectNode format = root.putObject("text").putObject("format");
        format.put("type", "json_schema");
        format.put("name", "scenario_batch");
        format.put("strict", false);
        format.set("schema", buildJsonSchema());

        return root;
    }

    private ObjectNode buildJsonSchema() {
        ObjectNode scenarioSchema = objectMapper.createObjectNode();
        scenarioSchema.put("type", "object");

        ObjectNode scenarioProps = scenarioSchema.putObject("properties");
        scenarioProps.putObject("name").put("type", "string");

        ObjectNode categoryProp = scenarioProps.putObject("category");
        categoryProp.put("type", "string");
        ArrayNode categoryEnum = categoryProp.putArray("enum");
        for (ScenarioCategory category : ScenarioCategory.values()) {
            categoryEnum.add(category.name());
        }

        scenarioProps.putObject("purpose").put("type", "string");

        ObjectNode violatedRuleProp = scenarioProps.putObject("violatedRulePath");
        violatedRuleProp.putArray("type").add("string").add("null");

        scenarioProps.putObject("payload").put("type", "object");
        scenarioProps.putObject("expectedStatus").put("type", "integer");

        scenarioSchema.putArray("required")
                .add("name").add("category").add("purpose").add("violatedRulePath")
                .add("payload").add("expectedStatus");

        ObjectNode scenariosArray = objectMapper.createObjectNode();
        scenariosArray.put("type", "array");
        scenariosArray.set("items", scenarioSchema);

        ObjectNode rootSchema = objectMapper.createObjectNode();
        rootSchema.put("type", "object");
        ObjectNode rootProps = rootSchema.putObject("properties");
        rootProps.putObject("endpointId").put("type", "string");
        rootProps.set("scenarios", scenariosArray);
        rootSchema.putArray("required").add("endpointId").add("scenarios");

        return rootSchema;
    }
}
