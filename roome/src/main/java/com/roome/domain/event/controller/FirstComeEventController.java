package com.roome.domain.event.controller;

import com.roome.domain.event.service.FirstComeEventService;
import com.roome.global.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class FirstComeEventController {

  private final FirstComeEventService firstComeEventService;

  @PostMapping("/{eventId}/join")
  public ResponseEntity<String> joinEvent(@AuthenticatedUser Long userId, @PathVariable Long eventId) {
    firstComeEventService.joinEvent(userId, eventId);
    return ResponseEntity.ok("이벤트 참여 성공!");
  }
}
