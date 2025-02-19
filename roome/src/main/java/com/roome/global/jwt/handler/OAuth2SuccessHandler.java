package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2UserPrincipal oauth2User = (OAuth2UserPrincipal) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");

        // JWT 토큰 생성
        JwtToken token = jwtTokenProvider.createToken(authentication);

        // 응답 데이터 생성
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(oauth2User.getUser().getId())
                        .email(email)
                        .nickname(oauth2User.getUser().getNickname())
                        .email(oauth2User.getUser().getEmail())
                        .profileImage(oauth2User.getUser().getProfileImage())
                        .build())
                .build();

        // 응답 헤더 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Authorization", "Bearer " + token.getAccessToken());

        // 리프레시 토큰을 쿠키에 설정
        Cookie cookie = new Cookie("refresh_token", token.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 응답 바디 작성
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }
}
