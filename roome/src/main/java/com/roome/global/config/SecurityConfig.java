package com.roome.global.config;

import com.roome.domain.auth.service.CustomOAuth2UserService;
import com.roome.global.jwt.filter.JwtAuthenticationFilter;
import com.roome.global.jwt.handler.OAuth2AuthenticationFailureHandler;
import com.roome.global.jwt.handler.OAuth2AuthenticationSuccessHandler;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomOAuth2UserService oAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisService redisService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http

        // CSRF 보호 비활성화
        .csrf(csrf -> csrf.disable())

        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement((session) -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // X-Frame-Options 비활성화 (h2-console 접근 허용)
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))

        // OAuth2 로그인 설정
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(endpoint -> endpoint
                .baseUri("/oauth2/authorization")
            )
            .redirectionEndpoint(endpoint -> endpoint
                .baseUri("/oauth/callback/{registrationId}")
            )
            .userInfoEndpoint(endpoint -> endpoint
                .userService(oAuth2UserService)
            )
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
        )

        // 접근 제어 설정
        .authorizeHttpRequests((auth) -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(
                "/oauth2/authorization/**",  // OAuth 인증 시작 엔드포인트
                "/oauth/callback/**",        // OAuth 콜백 엔드포인트
                "/api/auth/user",            // 사용자 정보 조회만 허용
                "/api/auth/reissue-token", // 토큰 재발급
                "/error",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/mock/**",
                "/ws/**",
                "/docs/**"
            ).permitAll()
            .anyRequest().authenticated()
        )

        // 예외 처리
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
              response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 사용자입니다.");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
            })
        );

    // JWT 필터 추가
    http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisService),
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "https://desqb38rc2v50.cloudfront.net",
        "http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:63342"
    ));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}