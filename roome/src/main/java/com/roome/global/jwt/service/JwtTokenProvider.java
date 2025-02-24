package com.roome.global.jwt.service;

import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14; // 14일

    private SecretKey secretKey;

    // 테스트를 위한 setter
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Value("${spring.jwt.secret}")
    private String secret;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtToken createToken(String userId) {
        long now = (new Date()).getTime();

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "ACCESS")
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "REFRESH")
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return new JwtToken("Bearer", accessToken, refreshToken);
    }

    // 액세스 토큰 검증
    public boolean validateAccessToken(String token) {
        return validateToken(token, "ACCESS");
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(String token) {
        return validateToken(token, "REFRESH");
    }

    // 토큰 타입 확인
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get("type", String.class);
            if (!expectedType.equals(type)) {
                log.warn("[JWT 검증 실패] 잘못된 토큰 타입: {} (기대값: {})", type, expectedType);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("[JWT 검증 실패] {}", e.getMessage());
            return false;
        }
    }

    // 액세스 토큰에서 유저 ID 추출
    public String getUserIdFromToken(String accessToken) {
        return parseClaims(accessToken).getSubject();
    }

    public long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRE_TIME;
    }

    public long getAccessTokenExpirationTime() {
        return ACCESS_TOKEN_EXPIRE_TIME;
    }

    // Claims 파싱
    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("[JWT 만료] 만료된 토큰 접근 시도: {}", accessToken);
            throw new InvalidJwtTokenException();
        }
    }
}
