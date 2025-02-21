package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/mock/mycd/{myCdId}/comments")
public class MockCdCommentController {

  @Operation(summary = "Mock - CD 댓글 작성", description = "CD에 댓글을 작성합니다.")
  @PostMapping
  public ResponseEntity<CdCommentResponse> addComment(
      @PathVariable Long myCdId,
      @RequestBody CdCommentCreateRequest request
  ) {
    LocalDateTime createdAt = LocalDateTime.now();

    // Mock 사용자 정보 (하드코딩)
    Long mockUserId = 2L;

    CdCommentResponse response = new CdCommentResponse(
        new Random().nextLong(1, 100), // commentId 랜덤 생성
        myCdId,
        mockUserId,
        request.getTimestamp(), // 순서 수정
        request.getContent(),
        createdAt
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - 특정 CD에 작성된 댓글 조회", description = "특정 CD의 댓글 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<CdCommentListResponse> getComments(
      @PathVariable Long myCdId,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page
  ) {
    List<CdCommentResponse> mockData = List.of(
        new CdCommentResponse(1L, myCdId, 2L, "3:40", "이 곡 진짜 좋네요!", LocalDateTime.now()),
        new CdCommentResponse(2L, myCdId, 3L, "2:10", "현구님 추천 감사합니다!", LocalDateTime.now())
    );
    return ResponseEntity.ok(new CdCommentListResponse(mockData, page, 5, 12, 3));
  }

  @Operation(summary = "Mock - CD 댓글 검색", description = "특정 CD에 작성된 댓글을 검색합니다.")
  @GetMapping("/search")
  public ResponseEntity<CdCommentListResponse> searchComments(
      @PathVariable Long myCdId,
      @RequestParam("query") String keyword,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page
  ) {
    List<CdCommentResponse> mockData = List.of(
        new CdCommentResponse(1L, myCdId, 2L, "4:00", "이 곡 진짜 좋네요!", LocalDateTime.now()),
        new CdCommentResponse(2L, myCdId, 3L, "2:45", "현구님 추천 감사합니다!", LocalDateTime.now())
    );
    return ResponseEntity.ok(new CdCommentListResponse(mockData, page, 2, 2, 1));
  }

  @Operation(summary = "Mock - 단일 CD 댓글 삭제", description = "CD의 특정 댓글을 삭제합니다.")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long myCdId,
      @PathVariable Long commentId
  ) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Mock - 다중 CD 댓글 삭제", description = "CD의 여러 댓글을 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<Void> deleteMultipleComments(
      @PathVariable Long myCdId,
      @RequestParam List<Long> commentIds
  ) {
    return ResponseEntity.noContent().build();
  }
}
