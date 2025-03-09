package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "서평 API", description = "사용자가 보유한 도서에 대한 서평 관리 API - 등록, 조회, 수정, 삭제)")
@RestController
@RequiredArgsConstructor
public class MyBookReviewController {

  private final MyBookReviewService myBookReviewService;

  @Operation(summary = "서평 등록", description = "서평을 등록할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "서평 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음 (BOOK_NOT_FOUND)")
  })
  @PostMapping("/api/mybooks/{myBookId}/review")
  public ResponseEntity<MyBookReviewResponse> create(
      @AuthenticationPrincipal Long loginUserId,
      @Parameter(description = "서평을 등록할 책 ID")
      @PathVariable("myBookId") Long myBookId,
      @RequestBody MyBookReviewCreateRequest request
  ) {
    MyBookReviewResponse response = myBookReviewService.create(loginUserId, myBookId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "서평 조회", description = "서평을 조회할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "서평 조회 성공"),
      @ApiResponse(responseCode = "404", description = "해당 도서에 대한 서평이 없음 (REVIEW_NOT_FOUND)")
  })
  @GetMapping("/api/mybooks/{myBookId}/review")
  public ResponseEntity<MyBookReviewResponse> read(
      @Parameter(description = "조회할 책 ID")
      @PathVariable("myBookId") Long myBookId
  ) {
    MyBookReviewResponse response = myBookReviewService.read(myBookId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "서평 수정", description = "서평을 수정할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "서평 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "403", description = "다른 사용자의 서평 수정 불가 (FORBIDDEN)"),
      @ApiResponse(responseCode = "404", description = "서평을 찾을 수 없음 (REVIEW_NOT_FOUND)")
  })
  @PatchMapping("/api/mybooks/{myBookId}/review")
  public ResponseEntity<MyBookReviewResponse> update(
      @AuthenticationPrincipal Long loginUserId,
      @Parameter(description = "수정할 책 ID")
      @PathVariable("myBookId") Long myBookId,
      @RequestBody MyBookReviewUpdateRequest request
  ) {
    MyBookReviewResponse response = myBookReviewService.update(loginUserId, myBookId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "서평 삭제", description = "서평을 삭제할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "서평 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "403", description = "다른 사용자의 서평 삭제 불가 (FORBIDDEN)"),
      @ApiResponse(responseCode = "404", description = "서평을 찾을 수 없음 (REVIEW_NOT_FOUND)")
  })
  @DeleteMapping("/api/mybooks/{myBookId}/review")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal Long loginUserId,
      @Parameter(description = "삭제할 책 ID")
      @PathVariable("myBookId") Long myBookId
  ) {
    myBookReviewService.delete(loginUserId, myBookId);
    return ResponseEntity.ok().build();
  }
}
