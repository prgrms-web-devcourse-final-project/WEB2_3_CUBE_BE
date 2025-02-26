package com.roome.global.jwt.handler;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @Mock
    private RedirectStrategy redirectStrategy;

    @BeforeEach
    void setUp() {
        // 테스트를 위한 redirectUri 설정
        ReflectionTestUtils.setField(successHandler, "redirectUri", "http://localhost:5173/oauth/callback");
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("인증 성공 시 JWT 토큰이 발급되고 Redis에 저장되며, 프론트엔드로 리다이렉트된다")
    void onAuthenticationSuccess_GeneratesTokenAndRedirects() throws IOException {
        // Given
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("testuser")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .status(Status.OFFLINE)
                .lastLogin(LocalDateTime.now())
                .build();

        OAuth2UserPrincipal oAuth2UserPrincipal = mock(OAuth2UserPrincipal.class);
        when(oAuth2UserPrincipal.getUser()).thenReturn(user);

        Authentication authentication = new TestingAuthenticationToken(oAuth2UserPrincipal, null);

        JwtToken jwtToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .build();

        // Mocking
        when(jwtTokenProvider.createToken(anyString())).thenReturn(jwtToken);
        doNothing().when(redisService).saveRefreshToken(anyString(), anyString(), anyLong());
        doNothing().when(redirectStrategy).sendRedirect(any(), any(), anyString());

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(jwtTokenProvider).createToken(user.getId().toString());
        verify(redisService).saveRefreshToken(
                eq(user.getId().toString()),
                eq(jwtToken.getRefreshToken()),
                anyLong()
        );

        // 리다이렉트 검증 - URL에 액세스 토큰과 사용자 ID가 포함되어야 함
        verify(redirectStrategy).sendRedirect(
                eq(request),
                eq(response),
                contains("accessToken=test-access-token")
        );
        verify(redirectStrategy).sendRedirect(
                eq(request),
                eq(response),
                contains("userId=1")
        );
    }
}