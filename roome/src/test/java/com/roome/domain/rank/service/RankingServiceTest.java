package com.roome.domain.rank.service;

import com.roome.domain.rank.entity.Ranking;
import com.roome.domain.rank.repository.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private RankingRepository rankingRepository;

    private Ranking testRanking;

    @BeforeEach
    void setUp() {
        testRanking = Ranking.builder()
                .id(1L)
                .userId(1L)
                .score(100)
                .rankPosition(1)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("점수 업데이트 - 기존 유저 점수 추가")
    void updateScore_existingUser() {
        // Given
        when(rankingRepository.findByUserId(1L)).thenReturn(Optional.of(testRanking));

        // When
        rankingService.updateScore(1L, 50);

        // Then
        assertThat(testRanking.getScore()).isEqualTo(150);
        verify(rankingRepository, times(1)).save(testRanking);
    }

    @Test
    @DisplayName("점수 업데이트 - 새로운 유저 생성 및 점수 추가")
    void updateScore_newUser() {
        // Given
        when(rankingRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // When
        rankingService.updateScore(2L, 30);

        // Then
        verify(rankingRepository, times(1)).save(argThat(ranking ->
                ranking.getUserId().equals(2L) && ranking.getScore() == 30
        ));
    }

    @Test
    @DisplayName("랭킹 업데이트 - 점수 기준으로 순위 재정렬")
    void updateRanking() {
        // Given
        Ranking user1 = new Ranking(1L, 1L, 300, 0, LocalDateTime.now());
        Ranking user2 = new Ranking(2L, 2L, 200, 0, LocalDateTime.now());
        Ranking user3 = new Ranking(3L, 3L, 100, 0, LocalDateTime.now());

        List<Ranking> rankings = List.of(user1, user2, user3);
        when(rankingRepository.findTop10ByOrderByScoreDesc()).thenReturn(rankings);

        // When
        rankingService.updateRanking();

        // Then
        assertThat(user1.getRankPosition()).isEqualTo(1);
        assertThat(user2.getRankPosition()).isEqualTo(2);
        assertThat(user3.getRankPosition()).isEqualTo(3);

        verify(rankingRepository, times(3)).save(any(Ranking.class));
    }

    @Test
    @DisplayName("유저 랭킹 조회 - 존재하는 유저")
    void getUserRanking_existingUser() {
        // Given
        when(rankingRepository.findByUserId(1L)).thenReturn(Optional.of(testRanking));

        // When
        Optional<Ranking> ranking = rankingService.getUserRanking(1L);

        // Then
        assertThat(ranking).isPresent();
        assertThat(ranking.get().getScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("유저 랭킹 조회 - 존재하지 않는 유저")
    void getUserRanking_nonExistingUser() {
        // Given
        when(rankingRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // When
        Optional<Ranking> ranking = rankingService.getUserRanking(99L);

        // Then
        assertThat(ranking).isEmpty();
    }

    @Test
    @DisplayName("상위 10명 랭킹 조회")
    void getTopRankings() {
        // Given
        List<Ranking> topRankings = List.of(testRanking);
        when(rankingRepository.findTop10ByOrderByScoreDesc()).thenReturn(topRankings);

        // When
        List<Ranking> result = rankingService.getTopRankings();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("매주 월요일 00:00 점수 초기화")
    void resetWeeklyScores() {
        // Given
        Ranking user1 = new Ranking(1L, 1L, 300, 1, LocalDateTime.now());
        Ranking user2 = new Ranking(2L, 2L, 200, 2, LocalDateTime.now());
        List<Ranking> rankings = List.of(user1, user2);
        when(rankingRepository.findAll()).thenReturn(rankings);

        // When
        rankingService.resetWeeklyScores();

        // Then
        assertThat(user1.getScore()).isZero();
        assertThat(user2.getScore()).isZero();
        verify(rankingRepository, times(2)).save(any(Ranking.class));
    }
}
