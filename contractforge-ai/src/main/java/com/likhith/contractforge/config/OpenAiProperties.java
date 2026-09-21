package com.likhith.contractforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Where and how ContractForge calls OpenAI's Responses API. Bound from
 * {@code contractforge.openai.*} - the API key is never hard-coded anywhere,
 * only ever read from here (which itself reads {@code OPENAI_API_KEY} via
 * application.yml's {@code ${OPENAI_API_KEY:}} placeholder).
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "contractforge.openai")
public class OpenAiProperties {

    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey = "";
    private String model = "gpt-5-mini";
    private long timeoutMs = 30000;
    private int maxScenarios = 8;

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
