package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    @PostMapping("/api/mybooks")
    public ResponseEntity<MyBookResponse> create(
            @RequestParam("roomId") Long roomId,
            @RequestBody MyBookCreateRequest request
    ) {
        Long userId = 1L;
        MyBookResponse response = myBookService.create(userId, roomId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/mybooks/{myBookId}")
    public ResponseEntity<MyBookResponse> read(@PathVariable Long myBookId) {
        MyBookResponse response =  myBookService.read(myBookId);
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
            @RequestParam("roomId") Long roomId,
            @RequestParam String myBookIds
    ) {
        Long userId = 1L;
        myBookService.delete(userId, roomId, myBookIds);
        return ResponseEntity.ok().build();
    }
}
