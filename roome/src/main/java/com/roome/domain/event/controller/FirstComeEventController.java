package com.roome.domain.event.controller;

import com.roome.domain.event.dto.FirstComeEventResponse;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.service.FirstComeEventService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "선착순 이벤트 API", description = "선착순 이벤트 관련 API")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class FirstComeEventController {

  private final FirstComeEventService firstComeEventService;

  @Operation(summary = "선착순 이벤트 참여", description = "특정 이벤트에 참여한다.")
  @PostMapping("/{eventId}/join")
  public ResponseEntity<Void> joinEvent(
      @AuthenticatedUser Long userId,
      @PathVariable Long eventId) {
    firstComeEventService.joinEvent(userId, eventId);
    return ResponseEntity.noContent().build(); // 204 No Content 반환
  }

  @Operation(summary = "진행 중인 이벤트 조회", description = "현재 진행 중인 선착순 이벤트 정보를 반환한다.")
  @GetMapping("/ongoing")
  public ResponseEntity<FirstComeEventResponse> getOngoingEvent() {
    FirstComeEvent event = firstComeEventService.getOngoingEvent();
    return ResponseEntity.ok(FirstComeEventResponse.fromEntity(event));
  }

}
