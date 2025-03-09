package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
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
@Tag(name = "Mock - CdComment", description = "댓글 등록/조회/검색/삭제")
@RestController
@RequestMapping("/mock/my-cd")
public class MockCdCommentController {

  @Operation(summary = "Mock - CD 댓글 작성", description = "CD에 댓글을 작성합니다.")
  @PostMapping("/{myCdId}/comments")
  public ResponseEntity<CdCommentResponse> addComment(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @RequestBody CdCommentCreateRequest request
  ) {
    LocalDateTime createdAt = LocalDateTime.now();
    Long mockUserId = 2L;
    String mockNickname = "현구";

    CdCommentResponse response = new CdCommentResponse(
        new Random().nextLong(1, 100),
        myCdId,
        mockUserId,
        mockNickname,
        request.getTimestamp(),
        request.getContent(),
        createdAt
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - 특정 CD에 작성된 댓글 조회", description = "특정 CD의 댓글 목록을 조회합니다.")
  @GetMapping("/{myCdId}/comments")
  public ResponseEntity<CdCommentListResponse> getComments(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @Parameter(description = "한 번에 가져올 댓글 개수 (기본값: 99999)") @RequestParam(value = "size", required = false, defaultValue = "99999") int size
  ) {
    List<CdCommentResponse> mockData = List.of(
        new CdCommentResponse(1L, myCdId, 2L, "현구", 220, "이 곡 진짜 좋네요!", LocalDateTime.now()),
        // ✅ "3:40" → 220초
        new CdCommentResponse(2L, myCdId, 3L, "음악좋아하는사람", 130, "현구님 추천 감사합니다!", LocalDateTime.now())
        // ✅ "2:10" → 130초
    );

    return ResponseEntity.ok(new CdCommentListResponse(mockData, page, size, 12, 3));
  }

  @Operation(summary = "Mock - CD 댓글 검색", description = "특정 CD에 작성된 댓글을 검색합니다.")
  @GetMapping("/{myCdId}/comments/search")
  public ResponseEntity<CdCommentListResponse> searchComments(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @Parameter(description = "검색할 키워드", required = true) @RequestParam("query") String keyword,
      @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @Parameter(description = "한 번에 가져올 댓글 개수 (기본값: 5)") @RequestParam(value = "size", required = false, defaultValue = "5") int size
  ) {
    List<CdCommentResponse> mockData = List.of(
        new CdCommentResponse(1L, myCdId, 2L, "현구", 240, "이 곡 진짜 좋네요!", LocalDateTime.now()),
        // ✅ "4:00" → 240초
        new CdCommentResponse(2L, myCdId, 3L, "음악좋아하는사람", 165, "현구님 추천 감사합니다!", LocalDateTime.now())
        // ✅ "2:45" → 165초
    );

    return ResponseEntity.ok(new CdCommentListResponse(mockData, page, size, 2, 1));
  }

  @Operation(summary = "Mock - 단일 CD 댓글 삭제", description = "CD의 특정 댓글을 삭제합니다.")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable Long commentId
  ) {
    return ResponseEntity.noContent().build();
  }

}
