package com.roome.global.jwt.handler;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisService redisService;

  @Value("${app.oauth2.redirectUri}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2UserPrincipal oAuth2UserPrincipal = (OAuth2UserPrincipal) authentication.getPrincipal();
    User user = oAuth2UserPrincipal.getUser();

    log.info("OAuth2 로그인 성공: userId={}, email={}", user.getId(), user.getEmail());
    log.info("Request URI: {}", request.getRequestURI());
    log.info("Request URL: {}", request.getRequestURL());
    log.info("Query String: {}", request.getQueryString());

    // JWT 토큰 생성
    JwtToken jwtToken = jwtTokenProvider.createToken(user.getId().toString());

    // 리프레시 토큰을 쿠키에 저장
    ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token",
            jwtToken.getRefreshToken())
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(jwtTokenProvider.getRefreshTokenExpirationTime() / 1000)
        .sameSite("Lax")
        .build();
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    // 프론트엔드 리다이렉트 URI에 액세스 토큰을 쿼리 파라미터로 추가
    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("accessToken", jwtToken.getAccessToken())
        .build().toUriString();

    log.info("리다이렉트 URL: {}", targetUrl);

    // 헤더에 액세스 토큰 추가
    response.addHeader("Authorization", "Bearer " + jwtToken.getAccessToken());

    // 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}