package com.roome.domain.rank.service;

import com.roome.domain.point.entity.Point;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.rank.entity.UserActivity;
import com.roome.domain.rank.repository.UserActivityRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

  private final RedisTemplate<String, Object> redisTemplate;
  private final UserActivityRepository userActivityRepository;
  private final UserRepository userRepository;
  private final PointRepository pointRepository;

  // 최근 7일간의 활동 점수를 집계하여 Redis에 저장
  @Scheduled(fixedRate = 3600000) // 1시간마다 실행
  @Transactional(readOnly = true)
  public void updateRanking() {
    log.info("랭킹 갱신 작업 시작: {}", LocalDateTime.now());

    // 기존 랭킹 데이터 삭제
    redisTemplate.delete("user:ranking");

    // 최근 7일간 활동 데이터 집계
    LocalDateTime startDate = LocalDateTime.now().minusDays(7);

    // 사용자별 점수 집계
    Map<Long, Integer> userScores = new HashMap<>();

    List<UserActivity> activities = userActivityRepository.findAllByCreatedAtAfter(startDate);
    for (UserActivity activity : activities) {
      Long userId = activity.getUser().getId();
      userScores.put(userId, userScores.getOrDefault(userId, 0) + activity.getScore());
    }

    // Redis에 랭킹 데이터 저장
    for (Map.Entry<Long, Integer> entry : userScores.entrySet()) {
      redisTemplate.opsForZSet().add("user:ranking", entry.getKey().toString(), entry.getValue());
    }

    log.info("랭킹 갱신 완료: 사용자 {}명의 점수 업데이트", userScores.size());
  }


  // 매주 월요일 자정에 포인트 지급 및 점수 리셋 (상위 3명에게 포인트 지급)
  @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 자정
  @Transactional
  public void awardWeeklyPoints() {
    log.info("주간 랭킹 보상 지급 시작: {}", LocalDateTime.now());

    // 상위 랭킹 조회
    Set<ZSetOperations.TypedTuple<Object>> topRankers =
        redisTemplate.opsForZSet().reverseRangeWithScores("user:ranking", 0, 2);

    if (topRankers == null || topRankers.isEmpty()) {
      log.info("랭킹 데이터가 없습니다.");
      return;
    }

    // 전체 트랜잭션
    try {
      int rank = 0;
      for (ZSetOperations.TypedTuple<Object> ranker : topRankers) {
        String userId = (String) ranker.getValue();
        Double score = ranker.getScore();
        rank++;

        if (userId == null) {
          continue;
        }

        int points = 0;
        switch (rank) {
          case 1:
            points = 100;
            break;
          case 2:
            points = 70;
            break;
          case 3:
            points = 50;
            break;
        }

        // 포인트 지급
        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
        if (user == null) {
          continue;
        }

        // Point 엔티티 조회 or 생성
        Point pointEntity = pointRepository.findByUser(user)
            .orElseGet(() -> {
              // 포인트 엔티티가 없는 경우 새로 생성
              Point newPoint = Point.builder()
                  .user(user)
                  .balance(0)
                  .totalEarned(0)
                  .totalUsed(0)
                  .build();
              return pointRepository.save(newPoint);
            });

        // 포인트 적립
        pointEntity.addPoints(points);
        pointRepository.save(pointEntity);

        log.info("포인트 지급: 유저={}, 순위={}, 점수={}, 포인트={}",
            userId, rank, score != null ? score.intValue() : 0, points);
      }

      // 지난 주 활동 데이터 삭제
      LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
      userActivityRepository.deleteAllByCreatedAtBefore(oneWeekAgo);

      // 기존 랭킹 데이터 리셋
      redisTemplate.delete("user:ranking");
      log.info("주간 랭킹 보상 지급 완료 및 랭킹 리셋");
    } catch (IllegalArgumentException e) {
      // 잘못된 인자 예외
      log.error("주간 랭킹 보상 지급 중 데이터 오류: {}", e.getMessage(), e);
      throw e;
    } catch (DataAccessException e) {
      // 데이터베이스 접근 예외 (JPA나 Redis 관련)
      log.error("주간 랭킹 보상 지급 중 데이터베이스 접근 오류: {}", e.getMessage(), e);
    } catch (NullPointerException e) {
      // null 참조 예외 (Redis 데이터 형식 오류 등)
      log.error("주간 랭킹 보상 지급 중 null 오류: {}", e.getMessage(), e);
      throw new RuntimeException("랭킹 데이터가 불완전하거나 일부 정보가 누락되었습니다.", e);
    } catch (Exception e) {
      log.error("주간 랭킹 보상 지급 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("랭킹 보상 처리 중 시스템 오류가 발생했습니다", e);
    }
  }
}