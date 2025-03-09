package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "CD 댓글 API", description = "CD 댓글 관련 API - 댓글 작성, 조회, 삭제 기능 제공")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-cd/{myCdId}/comments")
public class CdCommentController {

  private final CdCommentService cdCommentService;

  @Operation(summary = "CD 댓글 작성", description = "사용자가 특정 CD에 대한 댓글을 작성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @PostMapping
  public ResponseEntity<CdCommentResponse> create(
      @AuthenticatedUser Long userId,
      @Parameter(description = "댓글을 추가할 CD의 ID", example = "1") @PathVariable Long myCdId,
      @RequestBody @Valid CdCommentCreateRequest request
  ) {
    CdCommentResponse response = cdCommentService.addComment(userId, myCdId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 댓글 목록 조회 및 검색", description = "특정 CD의 댓글 목록을 조회하거나 키워드를 포함하는 댓글을 검색합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
      @ApiResponse(responseCode = "404", description = "해당 CD에 댓글이 존재하지 않음")
  })
  @GetMapping
  public ResponseEntity<CdCommentListResponse> getComments(
      @Parameter(description = "댓글을 조회할 CD의 ID", example = "1")
      @PathVariable Long myCdId,
      @Parameter(description = "검색할 키워드 (입력하지 않으면 전체 조회)", example = "좋아요")
      @RequestParam(value = "keyword", required = false) String keyword,
      @Parameter(description = "페이지 번호", example = "0")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10") int size
  ) {
    CdCommentListResponse response = cdCommentService.getComments(myCdId, keyword, page, size);
    log.info("Received keyword: {}", keyword == null ? "null" : "'" + keyword + "'");

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 전체 댓글 목록 조회", description = "특정 CD의 모든 댓글을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "전체 댓글 조회 성공"),
      @ApiResponse(responseCode = "404", description = "해당 CD에 댓글이 존재하지 않음")
  })
  @GetMapping("/all")
  public ResponseEntity<List<CdCommentResponse>> getAllComments(
      @Parameter(description = "댓글을 조회할 CD의 ID") @PathVariable Long myCdId
  ) {
    List<CdCommentResponse> response = cdCommentService.getAllComments(myCdId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "CD 댓글 삭제", description = "특정 사용자가 자신이 작성한 댓글을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "해당 댓글이 존재하지 않음")
  })
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 댓글 ID", example = "1") @PathVariable Long commentId
  ) {
    cdCommentService.deleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }
}
