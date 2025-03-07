package com.roome.domain.point.controller;

import com.roome.domain.point.dto.PointBalanceResponse;
import com.roome.domain.point.dto.PointHistoryResponse;
import com.roome.domain.point.service.PointService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "포인트 API", description = "포인트 조회 및 내역 관리 API")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

  private final PointService pointService;

  @Operation(summary = "포인트 내역 조회", description = "사용자의 포인트 내역을 조회합니다. 최신순 정렬이며, 커서 기반 페이징을 지원합니다.")
  @GetMapping("/history")
  public ResponseEntity<PointHistoryResponse> getPointHistory(
      @AuthenticatedUser Long userId,
      @RequestParam @Parameter(description = "커서 값 (마지막 조회한 포인트 내역 ID)") Long cursor,
      @RequestParam @Parameter(description = "한 번에 조회할 포인트 내역 개수") int size) {

    PointHistoryResponse response = pointService.getPointHistory(userId, cursor, size);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "내 포인트 잔액 조회", description = "사용자의 현재 보유 포인트를 조회합니다.")
  @GetMapping("/balance")
  public ResponseEntity<PointBalanceResponse> getMyPointBalance(
      @AuthenticatedUser Long userId) {

    PointBalanceResponse balanceResponse = pointService.getMyPointBalance(userId);
    return ResponseEntity.ok(balanceResponse);
  }
}
