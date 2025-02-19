package com.roome.global.jwt.service;

import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueTokenSuccess() {
        // given
        String refreshToken = "valid_refresh_token";
        Claims claims = mock(Claims.class);
        User user = createUser(1L);
        JwtToken newToken = createJwtToken();

        given(jwtTokenProvider.validateToken(refreshToken))
                .willReturn(true);

        given(jwtTokenProvider.parseClaims(refreshToken))
                .willReturn(claims);

        given(claims.getSubject())
                .willReturn("1"); // userId

        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));

        given(jwtTokenProvider.createToken(any(Authentication.class)))
                .willReturn(newToken);

        // when
        JwtToken result = tokenService.reissueToken(refreshToken);

        // then
        assertNotNull(result);
        assertEquals(newToken.getAccessToken(), result.getAccessToken());
        assertEquals(newToken.getRefreshToken(), result.getRefreshToken());
        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시도 시 예외가 발생한다.")
    void reissueTokenFail_InvalidRefreshToken() {
        // given
        String invalidRefreshToken = "invalid_refresh_token";

        given(jwtTokenProvider.validateToken(invalidRefreshToken))
                .willReturn(false);

        // when & then
        assertThrows(InvalidRefreshTokenException.class, () ->
                tokenService.reissueToken(invalidRefreshToken));

        verify(jwtTokenProvider).validateToken(invalidRefreshToken);
        verify(jwtTokenProvider, never()).parseClaims(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 리프레시 토큰으로 재발급 시도 시 예외가 발생한다.")
    void reissueTokenFail_UserNotFound() {
        // given
        String refreshToken = "valid_refresh_token";
        Claims claims = mock(Claims.class);

        given(jwtTokenProvider.validateToken(refreshToken))
                .willReturn(true);

        given(jwtTokenProvider.parseClaims(refreshToken))
                .willReturn(claims);

        given(claims.getSubject())
                .willReturn("999"); // 존재하지 않는 userId

        given(userRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () ->
                tokenService.reissueToken(refreshToken)
        );
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name("Test User")
                .nickname("Tester")
                .provider(Provider.KAKAO)
                .providerId("test_provider_id")
                .status(Status.OFFLINE)
                .build();
    }

    private JwtToken createJwtToken() {
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();
    }
}
