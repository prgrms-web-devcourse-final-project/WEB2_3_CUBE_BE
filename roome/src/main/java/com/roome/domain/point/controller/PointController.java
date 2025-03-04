package com.roome.domain.point.controller;

import com.roome.domain.point.dto.PointBalanceResponse;
import com.roome.domain.point.dto.PointHistoryResponse;
import com.roome.domain.point.service.PointService;
import com.roome.global.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

  private final PointService pointService;

  @GetMapping("/history")
  public ResponseEntity<PointHistoryResponse> getPointHistory(
      @AuthenticatedUser Long userId,
      @RequestParam(defaultValue = "0") Long cursor,
      @RequestParam(defaultValue = "10") int size) {

    PointHistoryResponse response = pointService.getPointHistory(userId, cursor, size);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/balance")
  public ResponseEntity<PointBalanceResponse> getMyPointBalance(@AuthenticatedUser Long userId) {
    PointBalanceResponse balanceResponse = pointService.getMyPointBalance(userId);
    return ResponseEntity.ok(balanceResponse);
  }
}
