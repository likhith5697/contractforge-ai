package com.likhith.contractforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Where and how ContractForge fetches the banking API's OpenAPI document.
 * Bound from {@code contractforge.openapi.*} in application.yml (or environment
 * variables) so the source URL is never hard-coded in parsing/service logic.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "contractforge.openapi")
public class OpenApiSourceProperties {

    private String sourceUrl = "http://localhost:8080/v3/api-docs";
    private long connectTimeoutMs = 5000;
    private long readTimeoutMs = 10000;
}
