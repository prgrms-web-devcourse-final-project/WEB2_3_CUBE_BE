package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.*;
import com.roome.domain.guestbook.service.GuestbookService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Guestbook API", description = "방명록 관련 API")
@RestController
@RequestMapping("/api/guestbooks")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @Operation(summary = "방명록 조회", description = "주어진 방 ID에 대한 방명록을 페이지별로 조회")
    @GetMapping("/{roomId}")
    public ResponseEntity<GuestbookListResponseDto> getGuestbook(
            @PathVariable Long roomId,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(guestbookService.getGuestbook(roomId, page, size));
    }

    @Operation(summary = "방명록 추가", description = "주어진 방 ID에 방명록 추가")
    @PostMapping("/{roomId}")
    public ResponseEntity<GuestbookListResponseDto> addGuestbook(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "size", required = false, defaultValue = "2") int size,
            @Valid @RequestBody GuestbookRequestDto requestDto
    ) {
        return ResponseEntity.ok(guestbookService.addGuestbookWithPagination(roomId, userId, requestDto, size));
    }

    @Operation(summary = "방명록 삭제", description = "주어진 방명록 ID에 해당하는 방명록 삭제")
    @DeleteMapping("/{guestbookId}")
    public ResponseEntity<Void> deleteGuestbook(
            @PathVariable Long guestbookId,
            @AuthenticationPrincipal Long userId
    ) {
        guestbookService.deleteGuestbook(guestbookId, userId);
        return ResponseEntity.ok().build();
    }
}