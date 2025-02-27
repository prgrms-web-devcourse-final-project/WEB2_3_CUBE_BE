package com.roome.domain.rank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

  @InjectMocks
  private RankingService rankingService;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ZSetOperations<String, Object> zSetOperations;

  private User testUser;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    // 테스트 유저 생성
    testUser = User.builder().id(1L).nickname("테스트유저").profileImage("profile.jpg").build();
  }

  @Test
  @DisplayName("상위 10명 랭킹 조회")
  void getTopRankings() {
    // Given
    Set<ZSetOperations.TypedTuple<Object>> mockRankSet = new HashSet<>();

    // 3명의 유저
    mockRankSet.add(createTypedTuple("1", 100.0));
    mockRankSet.add(createTypedTuple("2", 200.0));
    mockRankSet.add(createTypedTuple("3", 300.0));

    when(zSetOperations.reverseRangeWithScores("user:ranking", 0, 9)).thenReturn(mockRankSet);

    // 유저 정보 모킹
    when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "유저1")));
    when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "유저2")));
    when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "유저3")));

    // When
    List<UserRankingDto> result = rankingService.getTopRankings();

    // Then
    assertThat(result).hasSize(3);

    // id별
    var userMap = result.stream().collect(Collectors.toMap(UserRankingDto::getUserId, dto -> dto));

    // 유저1
    assertThat(userMap.get(1L).getScore()).isEqualTo(100);
    assertThat(userMap.get(1L).getNickname()).isEqualTo("유저1");

    // 유저2
    assertThat(userMap.get(2L).getScore()).isEqualTo(200);
    assertThat(userMap.get(2L).getNickname()).isEqualTo("유저2");

    // 유저3
    assertThat(userMap.get(3L).getScore()).isEqualTo(300);
    assertThat(userMap.get(3L).getNickname()).isEqualTo("유저3");
  }

  @Test
  @DisplayName("랭킹 데이터가 없는 경우")
  void getTopRankings_emptyRanking() {
    // Given
    when(zSetOperations.reverseRangeWithScores("user:ranking", 0, 9)).thenReturn(new HashSet<>());

    // When
    List<UserRankingDto> result = rankingService.getTopRankings();

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("랭킹에 없는 사용자가 있는 경우")
  void getTopRankings_userNotFound() {
    // Given
    Set<ZSetOperations.TypedTuple<Object>> mockRankSet = new HashSet<>();
    mockRankSet.add(createTypedTuple("1", 300.0));
    mockRankSet.add(createTypedTuple("999", 200.0)); // 존재하지 않는 유저

    when(zSetOperations.reverseRangeWithScores("user:ranking", 0, 9)).thenReturn(mockRankSet);

    when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "유저1")));
    when(userRepository.findById(999L)).thenReturn(Optional.empty()); // 유저 없음

    // When
    List<UserRankingDto> result = rankingService.getTopRankings();

    // Then
    assertThat(result).hasSize(1); // 존재하는 유저만 포함
    assertThat(result.get(0).getUserId()).isEqualTo(1L);
    assertThat(result.get(0).getNickname()).isEqualTo("유저1");
  }

  @Test
  @DisplayName("랭킹 데이터에 null 값이 있는 경우")
  void getTopRankings_nullValues() {
    // Given
    Set<ZSetOperations.TypedTuple<Object>> mockRankSet = new HashSet<>();
    mockRankSet.add(createTypedTuple(null, 300.0));
    mockRankSet.add(createTypedTuple("1", 200.0));

    when(zSetOperations.reverseRangeWithScores("user:ranking", 0, 9)).thenReturn(mockRankSet);

    when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "유저1")));

    // When
    List<UserRankingDto> result = rankingService.getTopRankings();

    // Then
    assertThat(result).hasSize(1); // null이 아닌 유저만 포함
    assertThat(result.get(0).getUserId()).isEqualTo(1L);
  }

  // TypedTuple 생성
  private ZSetOperations.TypedTuple<Object> createTypedTuple(String value, Double score) {
    return new ZSetOperations.TypedTuple<Object>() {
      @Override
      public Object getValue() {
        return value;
      }

      @Override
      public Double getScore() {
        return score;
      }

      @Override
      public int compareTo(ZSetOperations.TypedTuple<Object> o) {
        return getScore().compareTo(o.getScore());
      }
    };
  }

  // 테스트 유저 생성
  private User createUser(Long id, String nickname) {
    return User.builder().id(id).nickname(nickname).profileImage("profile_" + id + ".jpg").build();
  }
}