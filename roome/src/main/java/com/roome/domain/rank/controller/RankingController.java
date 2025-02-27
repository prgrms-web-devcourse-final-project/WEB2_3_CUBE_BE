package com.roome.domain.rank.controller;

import com.roome.domain.rank.dto.UserRankingDto;
import com.roome.domain.rank.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Tag(name = "랭킹", description = "랭킹 관련 API")
public class RankingController {

  private final RankingService rankingService;

  @Operation(
      summary = "상위 랭킹 조회",
      description = "포인트 기준 상위 10명의 사용자 랭킹을 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "랭킹 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<List<UserRankingDto>> getTopRankings() {
    return ResponseEntity.ok(rankingService.getTopRankings());
  }
}