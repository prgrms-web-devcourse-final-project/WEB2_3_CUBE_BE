package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Jwt 생성
        JwtToken token = jwtTokenProvider.createToken(authentication);

        // 쿠키에 Refresh Token 저장
        Cookie refreshTokenCookie = new Cookie("refresh_token", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        // Access Token을 헤더에 추가
        response.addHeader("Authorization", "Bearer " + token.getAccessToken());

        Map<String, Object> tokenResponse = Map.of(
                "access_token", token.getAccessToken(),
                "token_type", token.getGrantType(),
                "expires_in", 3600,
                "message", "토큰이 발급되었습니다."
        );

        // Access Token을 json으로 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(tokenResponse));
    }
}
