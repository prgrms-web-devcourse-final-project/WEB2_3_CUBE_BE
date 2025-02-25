package com.roome.global.jwt.service;

import com.roome.global.service.RedisService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String secret;
    private SecretKey secretKey;
    private String testEmail;
    private String token;

    @BeforeEach
    void setUp() {
        secret = "testsecretkeytestsecretkeytestsecretkeytestsecretkey";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        jwtTokenProvider.setSecretKey(secretKey);
        testEmail = "test@example.com";

        token = Jwts.builder()
                .setSubject("userId")
                .claim("email", testEmail)
                .claim("type", "ACCESS")
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();
    }

    @Test
    @DisplayName("유효한 JWT 액세스 토큰을 검증한다")
    void testValidateAccessToken() {
        when(redisService.isBlacklisted(anyString())).thenReturn(false);

        boolean isValid = jwtTokenProvider.validateAccessToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰은 유효하지 않음")
    void testBlacklistedTokenIsInvalid() {
        // 블랙리스트에 등록된 것으로 설정
        when(redisService.isBlacklisted(token)).thenReturn(true);

        boolean isValid = jwtTokenProvider.validateAccessToken(token);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰의 남은 시간은 0이다")
    void testExpiredTokenTimeToLiveIsZero() {
        String expiredToken = Jwts.builder()
                .setSubject("userId")
                .claim("type", "ACCESS")
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 이미 만료됨
                .signWith(secretKey)
                .compact();

        long timeToLive = jwtTokenProvider.getTokenTimeToLive(expiredToken);
        assertThat(timeToLive).isEqualTo(0);
    }
}