package com.roome.global.jwt.service;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.dto.JwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

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
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();
    }

    @Test
    @DisplayName("JWT 토큰에서 이메일을 추출한다")
    void testGetEmailFromToken() {
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("유효한 JWT 토큰을 검증한다")
    void testValidateToken() {
        boolean isValid = jwtTokenProvider.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("JWT 토큰에 이메일 클레임이 포함되는지 검증")
    void testCreateTokenWithEmailClaim() {
        // given
        Map<String, Object> attributes = Map.of(
                "id", "test_id",
                "kakao_account", Map.of(
                        "email", testEmail,
                        "profile", Map.of(
                                "nickname", "Test User",
                                "profile_image_url", "test_profile.jpg"
                        )
                )
        );

        User user = User.builder()
                .id(1L)
                .email(testEmail)
                .build();

        OAuth2Response oAuth2Response = OAuth2Provider.KAKAO.createOAuth2Response(attributes);
        OAuth2UserPrincipal userPrincipal = new OAuth2UserPrincipal(user, oAuth2Response);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        // when
        JwtToken token = jwtTokenProvider.createToken(authentication);

        // then
        Claims claims = jwtTokenProvider.parseClaims(token.getAccessToken());
        assertThat(claims.get("email", String.class)).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("OAuth2UserPrincipal에서 이메일 추출")
    void testGetEmailFromOAuth2UserPrincipal() {
        Map<String, Object> attributes = Map.of(
                "id", "test_id",
                "kakao_account", Map.of(
                        "email", testEmail,
                        "profile", Map.of(
                                "nickname", "Test User",
                                "profile_image_url", "test_profile.jpg"
                        )
                )
        );

        User user = User.builder()
                .id(1L)
                .email(testEmail)
                .build();

        OAuth2Response oAuth2Response = OAuth2Provider.KAKAO.createOAuth2Response(attributes);
        OAuth2UserPrincipal userPrincipal = new OAuth2UserPrincipal(user, oAuth2Response);

        String email = userPrincipal.getEmail();
        assertThat(email).isEqualTo(testEmail);
    }
}