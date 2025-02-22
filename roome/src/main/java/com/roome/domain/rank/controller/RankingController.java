package com.roome.domain.rank.controller;

import com.roome.domain.rank.entity.Ranking;
import com.roome.domain.rank.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserRanking(@PathVariable Long userId) {
        Optional<Ranking> ranking = rankingService.getUserRanking(userId);
        return ranking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/top")
    public ResponseEntity<List<Ranking>> getTopRankings() {
        return ResponseEntity.ok(rankingService.getTopRankings());
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<String> updateScore(@PathVariable Long userId, @RequestParam int points) {
        rankingService.updateScore(userId, points);
        return ResponseEntity.ok("점수가 업데이트되었습니다.");
    }
}
