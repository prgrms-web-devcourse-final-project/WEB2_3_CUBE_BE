package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "도서 API", description = "도서 등록/조회/삭제 API")
@RestController
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    @Operation(summary = "도서 등록", description = "도서를 등록할 수 있다.")
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
    @GetMapping("/api/mybooks/{myBookId}")
    public ResponseEntity<MyBookResponse> read(@PathVariable Long myBookId) {
        MyBookResponse response = myBookService.read(myBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "등록 도서 목록 조회", description = "등록 도서의 목록을 무한 스크롤 방식으로 조회할 수 있다.")
    @GetMapping("/api/mybooks")
    public ResponseEntity<MyBooksResponse> readAll(
            @RequestParam("userId") Long userId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId
    ) {
        MyBooksResponse response = myBookService.readAll(userId, pageSize, lastMyBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "등록 도서 삭제", description = "등록 도서를 삭제할 수 있다.")
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
