package com.company.auth_service.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {

        return new OpenAPI()

                /* Basic Info */
                .info(new Info()
                        .title("Auth Service API")
                        .description("Authentication & Authorization APIs for Ecommerce Platform")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Backend Team")
                                .email("backend@company.com")
                        )
                )

                /* JWT Security Scheme */
                .addSecurityItem(
                        new SecurityRequirement().addList("bearerAuth")
                )

                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
