package com.roome.domain.rank.service;

import com.roome.domain.rank.entity.Ranking;
import com.roome.domain.rank.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    // TODO: Redis 활용 예정
    // 점수 업데이트
    @Transactional
    public void updateScore(Long userId, int points) {
        Ranking ranking = rankingRepository.findByUserId(userId)
                .orElseGet(() -> rankingRepository.save(new Ranking(null, userId, 0, 0, LocalDateTime.now())));

        ranking.addScore(points);
        rankingRepository.save(ranking);

        updateRanking();
    }

    // 현재 순위 업데이트
    @Transactional
    public void updateRanking() {
        List<Ranking> rankings = rankingRepository.findTop10ByOrderByScoreDesc();
        int rank = 1;
        for (Ranking ranking : rankings) {
            ranking.setRankPosition(rank++);
            rankingRepository.save(ranking);
        }
    }

    // 유저 랭킹 조회
    public Optional<Ranking> getUserRanking(Long userId) {
        return rankingRepository.findByUserId(userId);
    }

    // 상위 10명 랭킹 조회
    public List<Ranking> getTopRankings() {
        return rankingRepository.findTop10ByOrderByScoreDesc();
    }

    // TODO: 배치 처리 도입 예정
    // 매주 월요일 00:00에 점수 리셋
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void resetWeeklyScores() {
        List<Ranking> rankings = rankingRepository.findAll();
        for (Ranking ranking : rankings) {
            ranking.resetScore();
            rankingRepository.save(ranking);
        }
    }
}
