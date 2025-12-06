package com.senibo.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for User Service API documentation.
 * Provides interactive API documentation with JWT authentication support.
 */
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI userServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("User Service API")
            .description("Event Booking System - User Service API Documentation")
            .version("1.0.0")
            .contact(new Contact()
                .name("Senibo")
                .email("senibodonpedro@gmail.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
  }

  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .bearerFormat("JWT")
        .scheme("bearer");
  }
}