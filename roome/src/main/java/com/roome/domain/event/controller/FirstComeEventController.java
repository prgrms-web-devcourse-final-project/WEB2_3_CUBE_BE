package com.roome.domain.event.controller;

import com.roome.domain.event.dto.FirstComeEventResponse;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.service.FirstComeEventService;
import com.roome.global.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class FirstComeEventController {

  private final FirstComeEventService firstComeEventService;

  @PostMapping("/{eventId}/join")
  public ResponseEntity<Void> joinEvent(
      @AuthenticatedUser Long userId,
      @PathVariable Long eventId) {
    firstComeEventService.joinEvent(userId, eventId);
    return ResponseEntity.noContent().build(); // 204 No Content 반환
  }

  @GetMapping("/ongoing")
  public ResponseEntity<FirstComeEventResponse> getOngoingEvent() {
    FirstComeEvent event = firstComeEventService.getOngoingEvent();
    return ResponseEntity.ok(FirstComeEventResponse.fromEntity(event));
  }

}
