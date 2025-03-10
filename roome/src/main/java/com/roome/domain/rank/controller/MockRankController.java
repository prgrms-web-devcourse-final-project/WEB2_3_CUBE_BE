package com.roome.domain.rank.controller;

import com.roome.domain.rank.dto.MockRankResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Slf4j
@RestController
@RequestMapping("/mock/rank")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "랭킹 조회")
public class MockRankController {

  @GetMapping
  public ResponseEntity<MockRankResponse> getMockRanking() {
    log.info("[Mock 랭킹 조회] 요청 받음");

    List<MockRankResponse.RankInfo> ranking = List.of(
        MockRankResponse.RankInfo.builder()
            .rank(1)
            .userId("999")
            .nickname("Mock User1")
            .profileImage("https://mock-image.com/profile1.png")
            .score(1200)
            .build(),
        MockRankResponse.RankInfo.builder()
            .rank(2)
            .userId("888")
            .nickname("Mock User2")
            .profileImage("https://mock-image.com/profile2.png")
            .score(1100)
            .build()
    );

    MockRankResponse response = MockRankResponse.builder()
        .ranking(ranking)
        .message("랭킹 조회 성공")
        .updateTime(LocalDateTime.now().toString())
        .build();

    return ResponseEntity.ok(response);
  }
}
