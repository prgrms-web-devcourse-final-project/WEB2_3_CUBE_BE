package com.roome.global.jwt.helper;

import com.roome.global.jwt.dto.JwtToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenResponseHelper {
    @Value("${jwt.refresh-token.expiration-time-seconds:1209600}")
    private int REFRESH_TOKEN_EXPIRE_TIME_SECONDS;

    public void setTokenResponse(HttpServletResponse response, JwtToken token) {
        setRefreshTokenCookie(response, token.getRefreshToken());
        setAccessTokenHeader(response, token.getAccessToken());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME_SECONDS)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.addHeader("Authorization", "Bearer " + accessToken);
    }

    public void removeTokenResponse(HttpServletResponse response) {

        // Refresh Token 쿠키 제거
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        // Authorization 헤더 제거
        response.setHeader("Authorization", "");
    }
}
