package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import com.roome.global.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-cd")
public class CdCommentController {

  private final CdCommentService cdCommentService;

  @PostMapping("/{myCdId}/comment")
  public ResponseEntity<CdCommentResponse> create(
      @AuthenticatedUser Long userId,
      @PathVariable Long myCdId,
      @RequestBody @Valid CdCommentCreateRequest request
  ) {
    CdCommentResponse response = cdCommentService.addComment(userId, myCdId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{myCdId}/comments")
  public ResponseEntity<CdCommentListResponse> getComments(
      @PathVariable Long myCdId,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "size", required = false, defaultValue = "99999") int size
  ) {
    CdCommentListResponse response = cdCommentService.getComments(myCdId, page, size);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{myCdId}/comments/search")
  public ResponseEntity<CdCommentListResponse> searchComments(
      @PathVariable Long myCdId,
      @RequestParam("query") String keyword,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "size", required = false, defaultValue = "5") int size
  ) {
    CdCommentListResponse response = cdCommentService.searchComments(myCdId, keyword, page, size);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticatedUser Long userId,
      @PathVariable Long commentId
  ) {
    cdCommentService.deleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/comments")
  public ResponseEntity<Void> deleteMultipleComments(
      @AuthenticatedUser Long userId,
      @RequestParam List<Long> commentIds
  ) {
    cdCommentService.deleteMultipleComments(userId, commentIds);
    return ResponseEntity.noContent().build();
  }
}
