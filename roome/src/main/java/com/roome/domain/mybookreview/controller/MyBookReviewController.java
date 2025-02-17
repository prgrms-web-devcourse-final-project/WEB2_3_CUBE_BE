package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyBookReviewController {

    private final MyBookReviewService myBookReviewService;

    @PostMapping("/api/mybooks-review")
    public ResponseEntity<MyBookReviewResponse> create(
            @RequestParam("myBookId") Long myBookId,
            @RequestBody MyBookReviewCreateRequest request
    ) {
        Long userId = 1L;
        MyBookReviewResponse response =  myBookReviewService.create(userId, myBookId, request);
        return ResponseEntity.ok(response);
    }
}
