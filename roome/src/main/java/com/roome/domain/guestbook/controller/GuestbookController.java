package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.*;
import com.roome.domain.guestbook.service.GuestbookService;
import com.roome.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guestbooks")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @GetMapping("/{roomId}")
    public ResponseEntity<GuestbookListResponseDto> getGuestbook(@PathVariable Long roomId,
                                                                 @RequestParam int page,
                                                                 @RequestParam int size) {
        return ResponseEntity.ok(guestbookService.getGuestbook(roomId, page, size));
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<GuestbookResponseDto> addGuestbook(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user, // JWT 인증된 사용자 정보
            @Valid @RequestBody GuestbookRequestDto requestDto
    ) {
        return ResponseEntity.ok(guestbookService.addGuestbook(roomId, user, requestDto));
    }

    @DeleteMapping("/{guestbookId}")
    public ResponseEntity<Void> deleteGuestbook(
            @PathVariable Long guestbookId,
            @AuthenticationPrincipal User user
    ) {
        guestbookService.deleteGuestbook(guestbookId, user);
        return ResponseEntity.ok().build();
    }
}