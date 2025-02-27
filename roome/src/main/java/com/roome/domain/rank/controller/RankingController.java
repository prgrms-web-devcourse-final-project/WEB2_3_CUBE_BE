package com.roome.domain.rank.controller;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.rank.service.RankingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  // top10 랭킹 조회
  @GetMapping
  public ResponseEntity<List<UserRankingDto>> getTopRankings() {
    return ResponseEntity.ok(rankingService.getTopRankings());
  }
}