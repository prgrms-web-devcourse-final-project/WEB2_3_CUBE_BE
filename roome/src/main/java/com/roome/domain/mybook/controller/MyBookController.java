package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    @PostMapping("/api/mybooks")
    public ResponseEntity<Void> create(
            @RequestParam("roomId") Long roomId,
            @RequestBody MyBookCreateRequest request
    ) {
        Long userId = 1L;
        myBookService.create(userId, roomId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/mybooks")
    public MyBooksResponse readAll(
            @RequestParam("roomId") Long roomId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId
    ) {
        return myBookService.readAll(roomId, pageSize, lastMyBookId);
    }
}
