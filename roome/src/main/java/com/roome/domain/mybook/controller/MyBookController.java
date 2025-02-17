package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
