package com.roome.global.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private final StringRedisTemplate redisTemplate;
  private static final String REFRESH_TOKEN_PREFIX = "RT:";
  private static final String BLACKLIST_PREFIX = "BL:";

  // Refresh Token 저장
  public void saveRefreshToken(String userId, String refreshToken, long expiration) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
      log.info("Refresh Token 저장 성공 - Key: {}, Token: {}, Expiration: {} ms", key, refreshToken,
          expiration);
    } catch (Exception e) {
      log.error("Refresh Token 저장 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("Refresh Token 저장 중 오류 발생", e);
    }
  }

  // Refresh Token 조회
  public String getRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      String refreshToken = redisTemplate.opsForValue().get(key);
      log.info("Refresh Token 조회 - Key: {}, Token: {}", key, refreshToken);
      return refreshToken;
    } catch (Exception e) {
      log.error("Refresh Token 조회 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("Refresh Token 조회 중 오류 발생", e);
    }
  }

  // Refresh Token 삭제
  public void deleteRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    try {
      redisTemplate.delete(key);
      log.info("Refresh Token 삭제 성공 - Key: {}", key);
    } catch (Exception e) {
      log.error("Refresh Token 삭제 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("Refresh Token 삭제 중 오류 발생", e);
    }
  }

  // 액세스 토큰 블랙리스트 추가
  public void addToBlacklist(String accessToken, long expiration) {
    String key = BLACKLIST_PREFIX + accessToken;
    try {
      redisTemplate.opsForValue().set(key, "logout", expiration, TimeUnit.MILLISECONDS);
      log.info("액세스 토큰 블랙리스트 추가 - Key: {}, Expiration: {} ms", key, expiration);
    } catch (Exception e) {
      log.error("액세스 토큰 블랙리스트 추가 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("액세스 토큰 블랙리스트 추가 중 오류 발생", e);
    }
  }

  // 블랙리스트 토큰 확인
  public boolean isBlacklisted(String accessToken) {
    String key = BLACKLIST_PREFIX + accessToken;
    try {
      boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
      log.info("블랙리스트 토큰 확인 - Key: {}, isBlacklisted: {}", key, isBlacklisted);
      return isBlacklisted;
    } catch (Exception e) {
      log.error("블랙리스트 토큰 확인 실패 - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new RuntimeException("블랙리스트 토큰 확인 중 오류 발생", e);
    }
  }
}