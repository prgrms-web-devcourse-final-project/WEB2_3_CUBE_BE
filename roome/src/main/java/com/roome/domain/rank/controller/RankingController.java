package com.roome.domain.rank.controller;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.rank.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Tag(name = "랭킹 API", description = "랭킹 시스템 관련 API")
public class RankingController {

  private final RankingService rankingService;

  @Operation(
      summary = "상위 랭킹 조회",
      description = "활동 점수 기준 상위 10명의 사용자 랭킹을 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "랭킹 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<List<UserRankingDto>> getTopRankings() {
    try {
      log.info("랭킹 조회 API 호출");
      List<UserRankingDto> rankings = rankingService.getTopRankings();
      log.info("랭킹 조회 성공: {} 명의 랭커 조회됨", rankings.size());
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("랭킹 조회 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }
}