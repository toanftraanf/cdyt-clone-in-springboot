package com.cdyt.be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .packagesToScan("com.cdyt.be")
        .build();
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title("CDYT API Clone")
        .description("CDYT API in Spring Boot")
        .version("v1.0")
        .contact(new Contact().name("Toan Tran")));
  }
}
