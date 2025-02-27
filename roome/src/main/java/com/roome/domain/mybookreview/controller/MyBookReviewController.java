package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookReviewController {

    private final MyBookReviewService myBookReviewService;

    @PostMapping("/api/mybooks-review")
    public ResponseEntity<MyBookReviewResponse> create(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam("myBookId") Long myBookId,
            @RequestBody MyBookReviewCreateRequest request
    ) {
        MyBookReviewResponse response = myBookReviewService.create(loginUserId, myBookId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks-review")
    public ResponseEntity<MyBookReviewResponse> read(@RequestParam("myBookId") Long myBookId) {
        MyBookReviewResponse response = myBookReviewService.read(myBookId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/mybooks-review/{myBookReviewId}")
    public ResponseEntity<MyBookReviewResponse> update(
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("myBookReviewId") Long myBookReviewId,
            @RequestBody MyBookReviewUpdateRequest request
    ) {
        MyBookReviewResponse response = myBookReviewService.update(loginUserId, myBookReviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/mybooks-review/{myBookReviewId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("myBookReviewId") Long myBookReviewId
    ) {
        myBookReviewService.delete(loginUserId, myBookReviewId);
        return ResponseEntity.ok().build();
    }
}
