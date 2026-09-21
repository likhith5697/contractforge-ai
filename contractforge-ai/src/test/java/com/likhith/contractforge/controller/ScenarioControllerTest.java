package com.likhith.contractforge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.likhith.contractforge.exception.LlmCommunicationException;
import com.likhith.contractforge.exception.LlmNotConfiguredException;
import com.likhith.contractforge.model.ScenarioGenerationResponse;
import com.likhith.contractforge.service.ScenarioGenerationService;

/**
 * Verifies the REST contract of ScenarioController, focusing on the HTTP
 * status mappings introduced in Phase 2 that no other test exercises yet:
 * request validation, missing API key, and LLM communication failures.
 * The actual scenario generation/validation logic is already covered by
 * ScenarioGenerationServiceTest.
 */
@WebMvcTest(ScenarioController.class)
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScenarioGenerationService scenarioGenerationService;

    @Test
    void blankEndpointIdReturns400() throws Exception {
        mockMvc.perform(post("/api/scenarios/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpointId\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void missingApiKeyReturns503() throws Exception {
        given(scenarioGenerationService.generate(org.mockito.ArgumentMatchers.any()))
                .willThrow(new LlmNotConfiguredException(
                        "OPENAI_API_KEY is not configured; scenario generation is unavailable"));

        mockMvc.perform(post("/api/scenarios/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpointId\": \"POST:/api/payments/scheduled\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("LLM_NOT_CONFIGURED"));
    }

    @Test
    void llmRateLimitMapsToTooManyRequests() throws Exception {
        given(scenarioGenerationService.generate(org.mockito.ArgumentMatchers.any()))
                .willThrow(new LlmCommunicationException(HttpStatus.TOO_MANY_REQUESTS, "LLM_RATE_LIMITED",
                        "OpenAI rate limit exceeded; try again later"));

        mockMvc.perform(post("/api/scenarios/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpointId\": \"POST:/api/payments/scheduled\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("LLM_RATE_LIMITED"));
    }

    @Test
    void successfulGenerationReturns200WithTheServiceResult() throws Exception {
        ScenarioGenerationResponse response = new ScenarioGenerationResponse(
                "POST:/api/payments/scheduled", "gpt-5-mini", List.of(), List.of(), 0, 0, Instant.now());
        given(scenarioGenerationService.generate(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(post("/api/scenarios/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpointId\": \"POST:/api/payments/scheduled\", \"maxScenarios\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("gpt-5-mini"));
    }
}
