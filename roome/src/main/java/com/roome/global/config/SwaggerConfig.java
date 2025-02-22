package com.roome.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {
    /**
     * OpenAPI Bean을 생성하여 애플리케이션의 OpenAPI 문서 구성을 제공합니다.
     *
     * @return OpenAPI OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        // Bearer 토큰 스키마
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 리프레시 토큰 쿠키 스키마
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("refresh_token");

        // 기본 Security 요청 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Roome API")
                        .description("Roome 프로젝트 API 명세서")
                        .version("v1.0.0"))
                // Security 설정
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                        .addSecuritySchemes("cookieAuth", cookieAuth))
                .security(Arrays.asList(securityRequirement));
    }
}