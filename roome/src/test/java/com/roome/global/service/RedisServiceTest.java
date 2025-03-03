package com.roome.global.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

  @InjectMocks
  private RedisService redisService;

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOps;

  private static final String USER_ID = "123";
  private static final String REFRESH_TOKEN = "refresh.token.123";
  private static final String ACCESS_TOKEN = "access.token.456";
  private static final long EXPIRATION = 3600000L; // 1시간

  @Test
  @DisplayName("Refresh Token 저장 성공")
  void saveRefreshTokenSuccess() {
    // given
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    // when
    redisService.saveRefreshToken(USER_ID, REFRESH_TOKEN, EXPIRATION);

    // then
    verify(valueOps, times(1)).set(eq("RT:" + USER_ID), eq(REFRESH_TOKEN), eq(EXPIRATION),
        eq(TimeUnit.MILLISECONDS));
  }

  @Test
  @DisplayName("Refresh Token 저장 실패")
  void saveRefreshTokenFailure() {
    // given
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    doThrow(new RedisConnectionFailureException("Connection refused")).when(valueOps)
        .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // when & then
    assertThatThrownBy(
        () -> redisService.saveRefreshToken(USER_ID, REFRESH_TOKEN, EXPIRATION)).isInstanceOf(
        RuntimeException.class).hasMessageContaining("Refresh Token 저장 중 오류 발생");
  }

  @Test
  @DisplayName("Refresh Token 조회 성공")
  void getRefreshTokenSuccess() {
    // given
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    when(valueOps.get("RT:" + USER_ID)).thenReturn(REFRESH_TOKEN);

    // when
    String result = redisService.getRefreshToken(USER_ID);

    // then
    assertThat(result).isEqualTo(REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Refresh Token 삭제 성공")
  void deleteRefreshTokenSuccess() {
    // given
    when(redisTemplate.delete("RT:" + USER_ID)).thenReturn(true);

    // when
    boolean result = redisService.deleteRefreshToken(USER_ID);

    // then
    assertThat(result).isTrue();
    verify(redisTemplate, times(1)).delete("RT:" + USER_ID);
  }

  @Test
  @DisplayName("Refresh Token 삭제 실패 - Redis 서버 문제")
  void deleteRefreshTokenFailure() {
    // given
    doThrow(new RedisConnectionFailureException("Connection refused")).when(redisTemplate)
        .delete(anyString());

    // when
    boolean result = redisService.deleteRefreshToken(USER_ID);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("블랙리스트 추가 성공")
  void addToBlacklistSuccess() {
    // given
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    // when
    boolean result = redisService.addToBlacklist(ACCESS_TOKEN, EXPIRATION);

    // then
    assertThat(result).isTrue();
    verify(valueOps, times(1)).set(eq("BL:" + ACCESS_TOKEN), eq("logout"), eq(EXPIRATION),
        eq(TimeUnit.MILLISECONDS));
  }

  @Test
  @DisplayName("블랙리스트 추가 실패")
  void addToBlacklistFailure() {
    // given
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    doThrow(new RedisConnectionFailureException("Connection refused")).when(valueOps)
        .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // when
    boolean result = redisService.addToBlacklist(ACCESS_TOKEN, EXPIRATION);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("블랙리스트 확인 성공 - 토큰이 블랙리스트에 있음")
  void isBlacklistedSuccess() {
    // given
    when(redisTemplate.hasKey("BL:" + ACCESS_TOKEN)).thenReturn(true);

    // when
    boolean result = redisService.isBlacklisted(ACCESS_TOKEN);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("블랙리스트 확인 성공 - 토큰이 블랙리스트에 없음")
  void isBlacklistedTokenNotInBlacklist() {
    // given
    when(redisTemplate.hasKey("BL:" + ACCESS_TOKEN)).thenReturn(false);

    // when
    boolean result = redisService.isBlacklisted(ACCESS_TOKEN);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("블랙리스트 확인 실패")
  void isBlacklistedFailure() {
    // given
    doThrow(new RedisConnectionFailureException("Connection refused")).when(redisTemplate)
        .hasKey(anyString());

    // when & then
    assertThatThrownBy(() -> redisService.isBlacklisted(ACCESS_TOKEN)).isInstanceOf(
        RuntimeException.class).hasMessageContaining("블랙리스트 토큰 확인 중 오류 발생");
  }
}