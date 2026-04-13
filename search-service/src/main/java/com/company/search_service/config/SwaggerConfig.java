package com.company.search_service.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI searchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Search Service API")
                        .description("Full-Text Product Search (Elasticsearch) - Ecommerce Platform").version("v1.0"));
    }
}
