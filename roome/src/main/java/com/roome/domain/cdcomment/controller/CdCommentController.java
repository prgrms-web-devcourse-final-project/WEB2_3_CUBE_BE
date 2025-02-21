package com.roome.domain.cdcomment.controller;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
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

}
