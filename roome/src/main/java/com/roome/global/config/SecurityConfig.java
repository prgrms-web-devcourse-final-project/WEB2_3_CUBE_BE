package com.roome.global.config;

import com.roome.global.jwt.filter.JwtAuthenticationFilter;
import com.roome.global.jwt.handler.OAuth2FailureHandler;
import com.roome.global.jwt.handler.OAuth2SuccessHandler;
import com.roome.domain.oauth2.service.CustomOAuth2UserService;
import com.roome.global.jwt.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf((auth) -> auth.disable())
                .formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(authorizationRequestRepository()))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated());

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}