package com.roome.global.service;

import com.roome.domain.rank.entity.ScoreUpdateTask;
import com.roome.domain.rank.entity.TaskStatus;
import com.roome.domain.rank.repository.ScoreUpdateTaskRepository;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private final StringRedisTemplate redisTemplate;
  private final RedissonClient redissonClient;
  private final ScoreUpdateTaskRepository scoreUpdateTaskRepository;
  private static final String REFRESH_TOKEN_PREFIX = "RT:";
  private static final String BLACKLIST_PREFIX = "BL:";
  private static final String RANKING_KEY = "user:ranking";

  // 분산 락 사용
  public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
      Supplier<T> operation) {
    RLock lock = redissonClient.getLock(lockKey);
    int retryCount = 3;
    long retryDelay = 100; // 초기 재시도 대기 시간

    while (retryCount-- > 0) {
      boolean isLockAcquired = false;
      try {
        isLockAcquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        if (isLockAcquired) {
          return operation.get();
        } else {
          log.warn("락 획득 실패 - Key: {}, 재시도 중...", lockKey);
          Thread.sleep(retryDelay);
          retryDelay *= 2; // 지수 백오프 적용
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("락 획득 중단 - Key: " + lockKey, e);
      } finally {
        if (isLockAcquired && lock.isHeldByCurrentThread()) {
          lock.unlock();
          log.debug("락 해제 - Key: {}", lockKey);
        }
      }
    }
    throw new RuntimeException("락 획득 실패 - Key: " + lockKey);
  }

  // 사용자 점수 업데이트
  @Async("rankingTaskExecutor")
  public CompletableFuture<Boolean> updateUserScoreAsync(Long userId, int score) {
    try {
      incrementUserScoreWithLock(userId, score);
      return CompletableFuture.completedFuture(true);
    } catch (Exception e) {
      log.error("비동기 점수 업데이트 실패: userId={}, score={}, error={}", userId, score, e.getMessage());

      try {
        // 중복 저장 방지를 위한 조건 확인
        ScoreUpdateTask existingTask = scoreUpdateTaskRepository.findByUserIdAndScoreAndStatus(
            userId, score, TaskStatus.FAILED);

        if (existingTask == null) {
          ScoreUpdateTask task = new ScoreUpdateTask(userId, score);
          task.setStatus(TaskStatus.FAILED);
          scoreUpdateTaskRepository.save(task);
          log.info("실패한 점수 업데이트 작업 저장: userId={}, score={}", userId, score);
        }
      } catch (Exception saveEx) {
        log.error("실패 작업 저장 중 오류: userId={}, score={}, error={}",
            userId, score, saveEx.getMessage());
      }

      return CompletableFuture.completedFuture(false);
    }
  }

  // 사용자 랭킹 점수 증가
  public void incrementUserScoreWithLock(Long userId, int score) {
    String lockKey = "lock:user:ranking:" + userId;
    try {
      executeWithLock(lockKey, 200, 2000, () -> {
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, String.valueOf(userId), score);
        log.debug("점수 업데이트 완료 - UserId: {}, Score: {}", userId, score);
        return null;
      });
    } catch (Exception e) {
      log.error("점수 업데이트 실패, 복구 대기열에 추가: userId={}, score={}", userId, score, e);
      // 실패한 작업 저장
      ScoreUpdateTask task = new ScoreUpdateTask(userId, score);
      task.setStatus(TaskStatus.FAILED);
      scoreUpdateTaskRepository.save(task);
      throw e;
    }
  }

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

  // 사용자 랭킹 데이터 삭제
  @Retryable(value = {
      RedisConnectionFailureException.class}, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public boolean deleteUserRankingData(String userId) {
    try {
      // 랭킹 Sorted Set에서 사용자 제거
      Long removed = redisTemplate.opsForZSet().remove(RANKING_KEY, String.valueOf(userId));
      boolean result = removed != null && removed > 0;

      // 사용자 총점 데이터 삭제
      String totalScoreKey = "user:total:" + userId;
      redisTemplate.delete(totalScoreKey);

      log.debug("사용자 랭킹 데이터 삭제 결과 - UserId: {}, 성공여부: {}", userId, result);
      return result;
    } catch (Exception e) {
      log.error("사용자 랭킹 데이터 삭제 실패 - UserId: {}, Error: {}", userId, e.getMessage(), e);
      return false;
    }
  }
}