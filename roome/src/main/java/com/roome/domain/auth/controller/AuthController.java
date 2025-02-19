package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.dto.response.LogoutResponse;
import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.exception.MissingAuthorizationCodeException;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {

            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                log.error("[로그인 실패] 요청에 authorization code가 없음 (provider: {})", provider);
                throw new MissingAuthorizationCodeException();
            }

            OAuth2Provider oAuth2Provider = OAuth2Provider.from(provider);

            // 로그인 처리
            LoginResponse loginResponse = oAuth2LoginService.login(oAuth2Provider, request.getCode());

            if (loginResponse == null) {
                log.error("[로그인 실패] 로그인 응답이 null임 (provider: {})", provider);
                throw new OAuth2AuthenticationProcessingException();
            }

            // JWT 발급 후 응답 헤더와 쿠키 설정
            tokenResponseHelper.setTokenResponse(response, new JwtToken(
                    loginResponse.getAccessToken(),
                    loginResponse.getRefreshToken(),
                    "Bearer"
            ));

            return ResponseEntity.ok(loginResponse);
        } catch (InvalidProviderException e) {
            log.error("[로그인 실패] 잘못된 provider 요청 (provider: {})", provider, e);
            throw e;
        } catch (OAuth2AuthenticationProcessingException e) {
            log.error("[로그인 실패] OAuth2 처리 중 오류 발생 (provider: {})", provider, e);
            throw e;
        } catch (Exception e) {
            log.error("Login failed for provider: {}", provider, e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // 헤더에서 액세스 토큰 가져오기
        String accessToken = extractToken(request);

        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            log.info("[로그아웃 성공] 유효한 액세스 토큰: {}", accessToken);
        } else {
            log.warn("[로그아웃 요청] 유효하지 않은 액세스 토큰");
        }

        // 토큰 무효화
        tokenResponseHelper.removeTokenResponse(response);

        return ResponseEntity.ok(new LogoutResponse("로그아웃 되었습니다."));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
