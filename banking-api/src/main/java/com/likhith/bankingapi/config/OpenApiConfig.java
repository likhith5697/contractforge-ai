package com.likhith.bankingapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Components;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API (Mock)")
                        .description("Synthetic mock banking-platform API suite covering customer onboarding, "
                                + "accounts, transfers, payments, cards, loans, KYC, fraud, disputes, "
                                + "notifications, consents and statements. All data is synthetic and used for "
                                + "API contract testing purposes only - no real PII or financial data.")
                        .version("1.0.0")
                        .contact(new Contact().name("ContractForge AI").email("likhith2kuv@gmail.com"))
                        .license(new License().name("Internal Mock Use Only")))
                .components(new Components()
                        .addParameters("XRequestId", new Parameter()
                                .in("header")
                                .name("X-Request-Id")
                                .description("Optional correlation id; a UUID is generated when omitted and echoed on the response")
                                .required(false)));
    }
}
