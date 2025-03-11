package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.service.GuestbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "방명록 API", description = "방명록 관련 API")
@RestController
@RequestMapping("/api/guestbooks")
@RequiredArgsConstructor
public class GuestbookController {

  private final GuestbookService guestbookService;

  @Operation(summary = "방명록 조회", description = "주어진 방 ID에 대한 방명록을 페이지별로 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방명록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음 (ROOM_NOT_FOUND)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{roomId}")
  public ResponseEntity<GuestbookListResponseDto> getGuestbook(
      @Parameter(description = "방 ID") @PathVariable Long roomId,
      @RequestParam int page,
      @RequestParam int size) {
    return ResponseEntity.ok(guestbookService.getGuestbook(roomId, page, size));
  }

  @Operation(summary = "방명록 추가", description = "주어진 방 ID에 방명록 추가")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방명록 추가 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음 (ROOM_NOT_FOUND)"),
      @ApiResponse(responseCode = "403", description = "방명록 작성 제한 초과 (LIMIT_EXCEEDED)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/{roomId}")
  public ResponseEntity<GuestbookListResponseDto> addGuestbook(
      @Parameter(description = "방 ID") @PathVariable Long roomId,
      @AuthenticationPrincipal Long userId,
      @Parameter(description = "조회할 방명록 개수 (기본값: 2)", example = "2")
      @RequestParam(value = "size", required = false, defaultValue = "2") int size,
      @Valid @RequestBody GuestbookRequestDto requestDto
  ) {
    return ResponseEntity.ok(
        guestbookService.addGuestbookWithPagination(roomId, userId, requestDto, size));
  }

  @Operation(summary = "방명록 삭제", description = "주어진 방명록 ID에 해당하는 방명록 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방명록 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "403", description = "권한 없음 (FORBIDDEN)"),
      @ApiResponse(responseCode = "404", description = "방명록을 찾을 수 없음 (GUESTBOOK_NOT_FOUND)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{guestbookId}")
  public ResponseEntity<Void> deleteGuestbook(
      @Parameter(description = "삭제할 방명록 ID") @PathVariable Long guestbookId,
      @AuthenticationPrincipal Long userId
  ) {
    guestbookService.deleteGuestbook(guestbookId, userId);
    return ResponseEntity.ok().build();
  }
}