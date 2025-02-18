package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import com.roome.global.jwt.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/api/mybooks")
    public ResponseEntity<MyBookResponse> create(
            HttpServletRequest httpServletRequest,
            @RequestParam("roomId") Long roomId,
            @RequestBody MyBookCreateRequest request
    ) {
        Long userId = getUserIdFrom(httpServletRequest);
        MyBookResponse response = myBookService.create(userId, roomId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks")
    public ResponseEntity<MyBooksResponse> readAll(
            @RequestParam("roomId") Long roomId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId
    ) {
        MyBooksResponse response = myBookService.readAll(roomId, pageSize, lastMyBookId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/mybooks")
    public ResponseEntity<Void> delete(
            HttpServletRequest httpServletRequest,
            @RequestParam("roomId") Long roomId,
            @RequestParam String myBookIds
    ) {
        Long userId = getUserIdFrom(httpServletRequest);
        myBookService.delete(userId, roomId, myBookIds);
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
