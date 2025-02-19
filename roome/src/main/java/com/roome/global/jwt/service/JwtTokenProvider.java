package com.roome.global.jwt.service;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidJwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;
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

    // User 정보로 Access Token 생성
    public JwtToken createToken(Authentication authentication) {
        long now = (new Date()).getTime();
        String userId = authentication.getName();
        String email = null;

        // OAuth2UserPrincipal에서 email 추출
        if (authentication.getPrincipal() instanceof OAuth2UserPrincipal) {
            email = ((OAuth2UserPrincipal) authentication.getPrincipal()).getEmail();
        }

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", Collections.emptyList());
    }

    // 토큰 정보 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("[JWT 검증 실패] 잘못된 서명: {}", token);
        } catch (ExpiredJwtException e) {
            log.warn("[JWT 만료] 만료된 토큰: {}", token);
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT 검증 실패] 지원되지 않는 토큰 형식: {}", token);
        } catch (IllegalArgumentException e) {
            log.warn("[JWT 검증 실패] 잘못된 JWT 입력: {}", token);
        }
        return false;
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

    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }
}
