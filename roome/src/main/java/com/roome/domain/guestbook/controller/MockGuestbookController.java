package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.dto.GuestbookResponseDto;
import com.roome.domain.guestbook.dto.PaginationDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Tag(name = "Mock - Guestbook API", description = "방명록 관련 Mock API")
@RestController
@RequestMapping("/mock/guestbooks")
public class MockGuestbookController {

  @Operation(summary = "Mock - 방명록 조회", description = "roomId에 해당하는 방명록 목록 조회 (페이징 포함)")
  @GetMapping("/{roomId}")
  public ResponseEntity<GuestbookListResponseDto> getMockGuestbook(
      @PathVariable Long roomId,
      @RequestParam int page,
      @RequestParam int size
  ) {
    List<GuestbookResponseDto> mockGuestbooks = List.of(
        new GuestbookResponseDto(1L, 123L, "VisitorA", "https://example.com/profileA.jpg",
            "방 정말 예쁘네요!", LocalDateTime.parse("2025-02-20T12:00:00"), "하우스메이트"),
        new GuestbookResponseDto(2L, 124L, "VisitorB", "https://example.com/profileB.jpg",
            "분위기가 너무 좋아요!", LocalDateTime.parse("2025-02-20T12:00:00"), "지나가던 나그네")

    );

    PaginationDto pagination = new PaginationDto(page, size, 5);
    GuestbookListResponseDto response = new GuestbookListResponseDto(roomId, mockGuestbooks,
        pagination);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - 방명록 작성", description = "roomId에 방명록 메시지를 추가")
  @PostMapping("/{roomId}")
  public ResponseEntity<GuestbookResponseDto> addMockGuestbook(
      @PathVariable Long roomId,
      @RequestBody GuestbookRequestDto requestDto
  ) {
    GuestbookResponseDto mockResponse = new GuestbookResponseDto(
        3L, 125L, "VisitorC", "https://example.com/profileC.jpg",
        requestDto.getMessage(), LocalDateTime.parse("2025-02-20T12:00:00"), "지나가던 나그네"
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(mockResponse);
  }

  @Operation(summary = "Mock - 방명록 삭제", description = "guestbookId에 해당하는 방명록 삭제")
  @DeleteMapping("/{guestbookId}")
  public ResponseEntity<String> deleteMockGuestbook(@PathVariable Long guestbookId) {
    return ResponseEntity.ok("ID: " + guestbookId);
  }
}
