package com.roome.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  /**
   * OpenAPI Bean을 생성하여 애플리케이션의 OpenAPI 문서 구성을 제공합니다.
   *
   * @return OpenAPI OpenAPI 설정 객체
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(
            new Info().title("Roome API").description("Roome 프로젝트 API 명세서").version("v1.0.0"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}