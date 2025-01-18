package com.bank.webservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi gatewayServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("gateway-service")
                .addOpenApiMethodFilter(method -> true)
                .pathsToMatch("/v3/api-docs")
                .build();
    }
}