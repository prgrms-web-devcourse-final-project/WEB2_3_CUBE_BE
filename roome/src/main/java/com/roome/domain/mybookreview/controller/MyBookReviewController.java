package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import com.roome.global.jwt.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookReviewController {

    private final MyBookReviewService myBookReviewService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/api/mybooks-review")
    public ResponseEntity<MyBookReviewResponse> create(
            HttpServletRequest httpServletRequest,
            @RequestParam("myBookId") Long myBookId,
            @RequestBody MyBookReviewCreateRequest request
    ) {
        Long userId = getUserIdFrom(httpServletRequest);
        MyBookReviewResponse response = myBookReviewService.create(userId, myBookId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks-review")
    public ResponseEntity<MyBookReviewResponse> read(@RequestParam("myBookId") Long myBookId) {
        MyBookReviewResponse response = myBookReviewService.read(myBookId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/mybooks-review/{myBookReviewId}")
    public ResponseEntity<MyBookReviewResponse> update(
            HttpServletRequest httpServletRequest,
            @PathVariable("myBookReviewId") Long myBookReviewId,
            @RequestBody MyBookReviewUpdateRequest request
    ) {
        Long userId = getUserIdFrom(httpServletRequest);
        MyBookReviewResponse response =  myBookReviewService.update(userId, myBookReviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/mybooks-review/{myBookReviewId}")
    public ResponseEntity<Void> delete(
            HttpServletRequest httpServletRequest,
            @PathVariable("myBookReviewId") Long myBookReviewId
    ) {
        Long userId = getUserIdFrom(httpServletRequest);
        myBookReviewService.delete(userId, myBookReviewId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFrom(HttpServletRequest httpServletRequest) {
        String accessToken = httpServletRequest.getHeader("Authorization");
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer")) {
            accessToken = accessToken.substring(7);
        }
        Claims claims = jwtTokenProvider.parseClaims(accessToken);
        return Long.valueOf(claims.getSubject());
    }
}
