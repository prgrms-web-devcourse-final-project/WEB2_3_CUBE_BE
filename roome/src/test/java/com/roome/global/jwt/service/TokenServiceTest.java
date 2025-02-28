package com.roome.global.jwt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.service.RedisService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RedisService redisService;

  @InjectMocks
  private TokenService tokenService;

  @Test
  @DisplayName("액세스 토큰 재발급 성공")
  void reissueAccessTokenSuccess() {
    // given
    String refreshToken = "valid_refresh_token";
    User user = createUser(1L);
    String newAccessToken = "new_access_token";

    // 리프레시 토큰 검증
    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);

    // 리프레시 토큰에서 userId 추출
    given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn("1");

    // Redis에서 저장된 Refresh Token 가져오기
    given(redisService.getRefreshToken("1")).willReturn(refreshToken);

    // DB에서 유저 조회
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // 새 액세스 토큰 발급
    given(jwtTokenProvider.createAccessToken(anyString())).willReturn(newAccessToken);

    // when
    String result = tokenService.reissueAccessToken(refreshToken);

    // then
    assertNotNull(result);
    assertEquals(newAccessToken, result);
    verify(jwtTokenProvider).validateRefreshToken(refreshToken);
    verify(userRepository).findById(1L);
  }

  @Test
  @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시도 시 예외가 발생한다.")
  void reissueAccessTokenFail_InvalidRefreshToken() {
    // given
    String invalidRefreshToken = "invalid_refresh_token";

    given(jwtTokenProvider.validateRefreshToken(invalidRefreshToken)).willReturn(false);

    // when & then
    assertThrows(InvalidRefreshTokenException.class,
        () -> tokenService.reissueAccessToken(invalidRefreshToken));

    verify(jwtTokenProvider).validateRefreshToken(invalidRefreshToken);
    verify(jwtTokenProvider, never()).parseClaims(any());
    verify(userRepository, never()).findById(any());
  }

  @Test
  @DisplayName("존재하지 않는 사용자의 리프레시 토큰으로 재발급 시도 시 예외가 발생한다.")
  void reissueAccessTokenFail_UserNotFound() {
    // given
    String refreshToken = "valid_refresh_token";

    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);

    given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn("999");

    given(userRepository.findById(999L)).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class, () -> tokenService.reissueAccessToken(refreshToken));
  }

  @Test
  @DisplayName("Redis에 저장된 토큰이 없거나 일치하지 않을 때 예외가 발생한다.")
  void reissueAccessTokenFail_RefreshTokenNotMatchInRedis() {
    // given
    String refreshToken = "valid_refresh_token";
    User user = createUser(1L);

    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);

    given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn("1");

    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // Redis에 저장된 토큰이 다르거나 없음
    given(redisService.getRefreshToken("1")).willReturn("different_refresh_token");  // 다른 토큰 반환

    // when & then
    assertThrows(InvalidRefreshTokenException.class,
        () -> tokenService.reissueAccessToken(refreshToken));
  }

  @Test
  @DisplayName("액세스 토큰에서 유효한 사용자 ID 추출")
  void getUserIdFromTokenSuccess() {
    // given
    String accessToken = "valid_access_token";

    given(jwtTokenProvider.getUserIdFromToken(accessToken)).willReturn("1");

    // when
    Long userId = tokenService.getUserIdFromToken(accessToken);

    // then
    assertEquals(1L, userId);
  }

  private User createUser(Long id) {
    return User.builder().id(id).name("Test User").nickname("Tester").provider(Provider.KAKAO)
        .providerId("test_provider_id").status(Status.OFFLINE).build();
  }
}