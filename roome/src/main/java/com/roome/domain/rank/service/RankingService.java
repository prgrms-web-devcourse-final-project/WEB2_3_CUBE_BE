package com.roome.domain.rank.service;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final UserRepository userRepository;

  private static final String RANKING_KEY = "user:ranking";

  // top10 랭킹 조회
  public List<UserRankingDto> getTopRankings() {
    Set<ZSetOperations.TypedTuple<Object>> rankSet = redisTemplate.opsForZSet()
        .reverseRangeWithScores(RANKING_KEY, 0, 9);

    List<UserRankingDto> rankingList = new ArrayList<>();

    if (rankSet == null || rankSet.isEmpty()) {
      return rankingList;
    }

    int rank = 0;
    for (ZSetOperations.TypedTuple<Object> tuple : rankSet) {
      String userId = (String) tuple.getValue();
      Double score = tuple.getScore();
      rank++;

      if (userId == null) {
        continue;
      }

      // 사용자 정보 조회
      User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
      if (user == null) {
        continue;
      }

      UserRankingDto dto = UserRankingDto.builder().rank(rank).userId(user.getId())
          .nickname(user.getNickname()).profileImage(user.getProfileImage())
          .score(score != null ? score.intValue() : 0).build();

      rankingList.add(dto);
    }

    return rankingList;
  }
}