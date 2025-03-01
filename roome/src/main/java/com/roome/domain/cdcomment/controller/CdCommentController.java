package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CD 댓글", description = "CD 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-cd")
public class CdCommentController {

  private final CdCommentService cdCommentService;

  @Operation(summary = "CD 댓글 작성", description = "사용자가 특정 CD에 대한 댓글을 작성합니다.")
  @PostMapping("/{myCdId}/comment")
  public ResponseEntity<CdCommentResponse> create(
      @AuthenticatedUser Long userId,
      @Parameter(description = "댓글을 추가할 CD의 ID", example = "1") @PathVariable Long myCdId,
      @RequestBody @Valid CdCommentCreateRequest request
  ) {
    CdCommentResponse response = cdCommentService.addComment(userId, myCdId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 댓글 목록 조회 및 검색", description = "특정 CD의 댓글 목록을 조회하거나 키워드를 포함하는 댓글을 검색합니다.")
  @GetMapping("/{myCdId}/comments")
  public ResponseEntity<CdCommentListResponse> getComments(
      @Parameter(description = "댓글을 조회할 CD의 ID", example = "1") @PathVariable Long myCdId,
      @Parameter(description = "검색할 키워드 (입력하지 않으면 전체 조회)", example = "좋아요")
      @RequestParam(value = "query", required = false) String keyword,
      @Parameter(description = "페이지 번호", example = "0")
      @RequestParam(value = "page", required = false) int page,
      @Parameter(description = "페이지 크기", example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10") int size
  ) {
    CdCommentListResponse response = cdCommentService.getComments(myCdId, keyword, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 전체 댓글 목록 조회", description = "특정 CD의 모든 댓글을 조회합니다.")
  @GetMapping("/{myCdId}/comments/all")
  public ResponseEntity<List<CdCommentResponse>> getAllComments(
      @Parameter(description = "댓글을 조회할 CD의 ID") @PathVariable Long myCdId
  ) {
    List<CdCommentResponse> response = cdCommentService.getAllComments(myCdId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 댓글 삭제", description = "특정 사용자가 자신이 작성한 댓글을 삭제합니다.")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 댓글 ID", example = "1") @PathVariable Long commentId
  ) {
    cdCommentService.deleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }
}
