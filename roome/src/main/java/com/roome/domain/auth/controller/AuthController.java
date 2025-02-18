package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2LoginService oAuth2LoginService;
    private final TokenResponseHelper tokenResponseHelper;

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {

        OAuth2Provider oAuth2Provider = OAuth2Provider.from(provider);

        // 로그인 처리
        LoginResponse loginResponse = oAuth2LoginService.login(oAuth2Provider, request.getCode());

        // JWT 발급 후 응답 헤더와 쿠키 설정
        JwtToken jwtToken = new JwtToken(loginResponse.getAccessToken(), loginResponse.getRefreshToken(), "Bearer");
        tokenResponseHelper.setTokenResponse(response, jwtToken);

        return ResponseEntity.ok(loginResponse);

        } catch (InvalidProviderException e) {
            log.error("Invalid provider: {}", provider, e);
            throw e;
        } catch (Exception e) {
            log.error("Login failed for provider: {}", provider, e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }
}
