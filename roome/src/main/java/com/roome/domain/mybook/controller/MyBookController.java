package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "도서 책장 API", description = "사용자가 보유한 도서 관리 API - 등록, 조회, 삭제")
@RestController
@RequiredArgsConstructor
public class MyBookController {

  private final MyBookService myBookService;

  @Operation(summary = "도서 등록", description = "사용자가 새로운 도서를 등록할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)")
  })
  @PostMapping("/api/mybooks")
  public ResponseEntity<MyBookResponse> create(
      @AuthenticationPrincipal Long loginUserId,
      @RequestParam("userId") Long userId,
      @RequestBody MyBookCreateRequest request
  ) {
    MyBookResponse response = myBookService.create(loginUserId, userId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "등록 도서 상세 조회", description = "등록 도서의 상세 정보를 조회할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 조회 성공"),
      @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음 (BOOK_NOT_FOUND)")
  })

  @GetMapping("/api/mybooks/{myBookId}")
  public ResponseEntity<MyBookResponse> read(
      @Parameter(description = "조회할 도서 ID")
      @PathVariable Long myBookId
  ) {
    MyBookResponse response = myBookService.read(myBookId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "등록 도서 목록 조회", description = "등록 도서의 목록을 무한 스크롤 방식으로 조회할 수 있다. 제목, 출판사, 저자에 대해 검색할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)")
  })
  @GetMapping("/api/mybooks")
  public ResponseEntity<MyBooksResponse> readAll(
      @RequestParam("userId") Long userId,
      @RequestParam("pageSize") Long pageSize,
      @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId,
      @RequestParam(value = "keyword", required = false) String keyword
  ) {
    MyBooksResponse response = myBookService.readAll(userId, pageSize, lastMyBookId, keyword);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "등록 도서 삭제", description = "등록 도서를 삭제할 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "403", description = "다른 사용자의 도서를 삭제할 수 없음 (FORBIDDEN)"),
      @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음 (BOOK_NOT_FOUND)")
  })
  @DeleteMapping("/api/mybooks")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal Long loginUserId,
      @RequestParam("userId") Long userId,
      @RequestParam String myBookIds
  ) {
    myBookService.delete(loginUserId, userId, myBookIds);
    return ResponseEntity.ok().build();
  }
}
