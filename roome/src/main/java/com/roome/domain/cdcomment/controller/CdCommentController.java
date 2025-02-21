package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CdCommentController {

  private final CdCommentService cdCommentService;

  @PostMapping("/api/myCd/{myCdId}/comment")
  public ResponseEntity<CdCommentResponse> create(
      @PathVariable Long myCdId,
      @RequestBody @Valid CdCommentCreateRequest request
  ) {
    Long userId = 1L; // 1주차에서는 임시로 userId = 1L 고정
    CdCommentResponse response = cdCommentService.addComment(userId, myCdId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/myCd/{myCdId}/comments")
  public ResponseEntity<CdCommentListResponse> getComments(
      @PathVariable Long myCdId,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "size", required = false, defaultValue = "99999") int size
  ) {
    CdCommentListResponse response = cdCommentService.getComments(myCdId, page, size);
    return ResponseEntity.ok(response);
  }

}
