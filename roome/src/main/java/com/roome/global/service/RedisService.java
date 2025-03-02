package com.roome.global.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private final StringRedisTemplate redisTemplate;
  private static final String REFRESH_TOKEN_PREFIX = "RT:";
  private static final String BLACKLIST_PREFIX = "BL:";

  // Refresh Token 저장
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
  public void saveRefreshToken(String userId, String refreshToken, long expiration) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
      log.debug("Refresh Token 저장 성공 - Key: {}, Expiration: {} ms", key, expiration);
    } catch (Exception e) {
      log.error("Refresh Token 저장 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("Refresh Token 저장 중 오류 발생", e);
    }
  }

  // Refresh Token 조회
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public String getRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      String refreshToken = redisTemplate.opsForValue().get(key);
      log.debug("Refresh Token 조회 - Key: {}, 존재여부: {}", key, refreshToken != null);
      return refreshToken;
    } catch (Exception e) {
      log.error("Refresh Token 조회 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("Refresh Token 조회 중 오류 발생", e);
    }
  }

  // Refresh Token 삭제
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public boolean deleteRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      Boolean deleted = redisTemplate.delete(key);
      boolean result = deleted != null && deleted;
      log.debug("Refresh Token 삭제 결과 - Key: {}, 성공여부: {}", key, result);
      return result;
    } catch (Exception e) {
      log.error("Refresh Token 삭제 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      return false;
    }
  }

  // 액세스 토큰 블랙리스트 추가
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public boolean addToBlacklist(String accessToken, long expiration) {
    String key = BLACKLIST_PREFIX + accessToken;
    try {
      redisTemplate.opsForValue().set(key, "logout", expiration, TimeUnit.MILLISECONDS);
      log.debug("블랙리스트 추가 완료 - Key: {}, Expiration: {} ms", key, expiration);
      return true;
    } catch (Exception e) {
      log.error("블랙리스트 추가 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      return false;
    }
  }

  // 토큰이 블랙리스트에 있는지 확인
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 2, backoff = @Backoff(delay = 300))
  public boolean isBlacklisted(String accessToken) {
    String key = BLACKLIST_PREFIX + accessToken;
    try {
      boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
      log.trace("블랙리스트 토큰 확인 - Key: {}, isBlacklisted: {}", key, isBlacklisted);
      return isBlacklisted;
    } catch (Exception e) {
      log.error("블랙리스트 토큰 확인 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("블랙리스트 토큰 확인 중 오류 발생", e);
    }
  }
}