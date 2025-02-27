package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    @PostMapping("/api/mybooks")
    public ResponseEntity<MyBookResponse> create(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam("userId") Long userId,
            @RequestBody MyBookCreateRequest request
    ) {
        MyBookResponse response = myBookService.create(loginUserId, userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks/{myBookId}")
    public ResponseEntity<MyBookResponse> read(@PathVariable Long myBookId) {
        MyBookResponse response = myBookService.read(myBookId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks")
    public ResponseEntity<MyBooksResponse> readAll(
            @RequestParam("userId") Long userId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId
    ) {
        MyBooksResponse response = myBookService.readAll(userId, pageSize, lastMyBookId);
        return ResponseEntity.ok(response);
    }

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
