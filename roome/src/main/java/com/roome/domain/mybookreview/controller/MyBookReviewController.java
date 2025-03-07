package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "서평 API", description = "서평 등록/조회/수정/삭제 API")
@RestController
@RequiredArgsConstructor
public class MyBookReviewController {

    private final MyBookReviewService myBookReviewService;

    @Operation(summary = "서평 등록", description = "서평을 등록할 수 있다.")
    @PostMapping("/mybooks/{myBookId}/review")
    public ResponseEntity<MyBookReviewResponse> create(
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("myBookId") Long myBookId,
            @RequestBody MyBookReviewCreateRequest request
    ) {
        MyBookReviewResponse response = myBookReviewService.create(loginUserId, myBookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "서평 조회", description = "서평을 조회할 수 있다.")
    @GetMapping("/mybooks/{myBookId}/review")
    public ResponseEntity<MyBookReviewResponse> read(@PathVariable("myBookId") Long myBookId) {
        MyBookReviewResponse response = myBookReviewService.read(myBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "서평 수정", description = "서평을 수정할 수 있다.")
    @PatchMapping("/mybooks/{myBookId}/review")
    public ResponseEntity<MyBookReviewResponse> update(
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("myBookId") Long myBookId,
            @RequestBody MyBookReviewUpdateRequest request
    ) {
        MyBookReviewResponse response = myBookReviewService.update(loginUserId, myBookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "서평 삭제", description = "서평을 삭제할 수 있다.")
    @DeleteMapping("/mybooks/{myBookId}/review")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("myBookId") Long myBookId
    ) {
        myBookReviewService.delete(loginUserId, myBookId);
        return ResponseEntity.ok().build();
    }
}
