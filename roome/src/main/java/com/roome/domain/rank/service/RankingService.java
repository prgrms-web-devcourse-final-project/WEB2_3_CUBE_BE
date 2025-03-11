package com.roome.domain.rank.service;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RankingService {

  @Qualifier("rankingRedisTemplate")
  private final RedisTemplate<String, String> rankingRedisTemplate;
  private final UserRepository userRepository;

  private static final String RANKING_KEY = "user:ranking";

  // 생성자를 통해 특정 RedisTemplate 주입
  public RankingService(
      @Qualifier("rankingRedisTemplate") RedisTemplate<String, String> rankingRedisTemplate,
      UserRepository userRepository) {
    this.rankingRedisTemplate = rankingRedisTemplate;
    this.userRepository = userRepository;
  }

  // top10 랭킹 조회
  public List<UserRankingDto> getTopRankings() {
    Set<ZSetOperations.TypedTuple<String>> rankSet = rankingRedisTemplate.opsForZSet()
        .reverseRangeWithScores(RANKING_KEY, 0, 9);

    // 유효한 사용자 정보
    List<UserRankingDto> validRankings = new ArrayList<>();

    if (rankSet == null || rankSet.isEmpty()) {
      return validRankings;
    }

    // 유효한 사용자 정보만 수집
    for (ZSetOperations.TypedTuple<String> tuple : rankSet) {
      String userIdStr = tuple.getValue();
      Double score = tuple.getScore();

      if (userIdStr == null || score == null) {
        continue;
      }

      try {
        Long userId = Long.valueOf(userIdStr);

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
          // 탈퇴한 사용자인 경우 Redis에서 해당 데이터 삭제
          Long removed = rankingRedisTemplate.opsForZSet().remove(RANKING_KEY, userIdStr);
          log.info("랭킹에서 탈퇴 사용자 데이터 자동 삭제: userId={}, 삭제됨={}", userIdStr, removed > 0);
          continue;
        }

        // 유효한 사용자 정보 수집
        UserRankingDto dto = UserRankingDto
            .builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImage(user.getProfileImage())
            .score(score.intValue())
            .build();

        validRankings.add(dto);
      } catch (NumberFormatException e) {
        log.warn("랭킹 데이터 처리 중 오류: 유효하지 않은 userId={}", userIdStr);
      }
    }

    // 최종 랭킹 리스트
    List<UserRankingDto> result = new ArrayList<>();

    for (int i = 0; i < validRankings.size(); i++) {
      UserRankingDto original = validRankings.get(i);
      int rank = i + 1;

      UserRankingDto dto = UserRankingDto.builder()
          .rank(rank)
          .userId(original.getUserId())
          .nickname(original.getNickname())
          .profileImage(original.getProfileImage())
          .score(original.getScore())
          .isTopRank(rank <= 3)  // 1~3위는 상위 랭커
          .build();

      result.add(dto);
    }

    return result;
  }

  public boolean isRanker(Long userId) {
    Set<ZSetOperations.TypedTuple<String>> rankers = rankingRedisTemplate.opsForZSet()
        .reverseRangeWithScores(RANKING_KEY, 0, 9);
    if (rankers == null || rankers.isEmpty()) {
      return false;
    }
    String userIdStr = String.valueOf(userId);
    return rankers.stream()
        .anyMatch(ranker -> userIdStr.equals(ranker.getValue()));
  }
}
