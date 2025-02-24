package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.*;
import com.roome.domain.guestbook.service.GuestbookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guestbooks")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @GetMapping("/{roomId}")
    public ResponseEntity<GuestbookListResponseDto> getGuestbook(
            @PathVariable Long roomId,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(guestbookService.getGuestbook(roomId, page, size));
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<GuestbookResponseDto> addGuestbook(
            @PathVariable Long roomId,
            @RequestParam("userId") Long userId, // JWT 인증된 사용자 정보
            @Valid @RequestBody GuestbookRequestDto requestDto
    ) {
        return ResponseEntity.ok(guestbookService.addGuestbook(roomId, userId, requestDto));
    }

    @DeleteMapping("/{guestbookId}")
    public ResponseEntity<Void> deleteGuestbook(
            @PathVariable Long guestbookId,
            @RequestParam("userId") Long userId
    ) {
        guestbookService.deleteGuestbook(guestbookId, userId);
        return ResponseEntity.ok().build();
    }
}