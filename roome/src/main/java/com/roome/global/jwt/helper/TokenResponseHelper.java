package com.roome.global.jwt.helper;

import com.roome.global.jwt.dto.JwtToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenResponseHelper {
    private static final int REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60; // 14일

    public void setTokenResponse(HttpServletResponse response, JwtToken token) {
        setRefreshTokenCookie(response, token.getRefreshToken());
        setAccessTokenHeader(response, token.getAccessToken());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(REFRESH_TOKEN_EXPIRE_TIME);
        response.addCookie(refreshTokenCookie);
    }

    private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.addHeader("Authorization", "Bearer " + accessToken);
    }
}
