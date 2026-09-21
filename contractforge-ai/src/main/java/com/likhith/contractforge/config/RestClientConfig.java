package com.likhith.contractforge.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

/**
 * RestClient beans used by ContractForge's two outbound HTTP integrations:
 * fetching the banking-api OpenAPI document, and calling OpenAI's Responses
 * API. Each gets its own timeouts from its own properties so a slow/unreachable
 * dependency fails fast instead of hanging the caller's request indefinitely.
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final OpenApiSourceProperties openApiSourceProperties;
    private final OpenAiProperties openAiProperties;

    @Bean
    public RestClient contractForgeRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(openApiSourceProperties.getConnectTimeoutMs()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(openApiSourceProperties.getReadTimeoutMs()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient openAiRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(openAiProperties.getTimeoutMs()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(openAiProperties.getTimeoutMs()));

        // No default Authorization header here - OpenAiResponsesClient sets it per request from
        // OpenAiProperties, since the key is security-sensitive and may not even be set at
        // bean-creation time in environments where the LLM feature is simply unused.
        return RestClient.builder()
                .baseUrl(openAiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
