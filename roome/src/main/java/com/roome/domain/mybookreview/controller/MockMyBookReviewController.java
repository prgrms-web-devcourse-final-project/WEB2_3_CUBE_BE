package com.roome.domain.mybookreview.controller;

import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mock/mybooks-review")
@Tag(name = "MyBookReview", description = "서평 등록/조회/수정/삭제")
public class MockMyBookReviewController {

    @Tag(name = "서평 등록", description = "서평을 등록할 수 있다.")
    @PostMapping()
    public ResponseEntity<MyBookReviewResponse> create(
            @RequestParam("myBookId") Long myBookId,
            @RequestBody MyBookReviewCreateRequest request
    ) {
        MyBookReviewResponse response = createMyBookReviewResponse(1L, "title");
        return ResponseEntity.ok(response);
    }

    @Tag(name = "서평 조회", description = "서평을 조회할 수 있다.")
    @GetMapping()
    public ResponseEntity<MyBookReviewResponse> read(@RequestParam("myBookId") Long myBookId) {
        MyBookReviewResponse response = createMyBookReviewResponse(1L, "title");
        return ResponseEntity.ok(response);
    }

    @Tag(name = "서평 수정", description = "서평을 수정할 수 있다.")
    @PatchMapping("/{myBookReviewId}")
    public ResponseEntity<MyBookReviewResponse> update(
            @PathVariable("myBookReviewId") Long myBookReviewId,
            @RequestBody MyBookReviewUpdateRequest request
    ) {
        MyBookReviewResponse response = createMyBookReviewResponse(myBookReviewId, "title");
        return ResponseEntity.ok(response);
    }

    @Tag(name = "서평 삭제", description = "서평을 삭제할 수 있다.")
    @DeleteMapping("/{myBookReviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable("myBookReviewId") Long myBookReviewId
    ) {
        return ResponseEntity.ok().build();
    }

    private MyBookReviewResponse createMyBookReviewResponse(Long myBookReviewId, String title) {
        return new MyBookReviewResponse(
                myBookReviewId,
                title,
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                CoverColor.BLUE,
                LocalDateTime.of(2025, 1, 1, 1, 1, 1).format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h시 mm분", Locale.KOREAN))
        );
    }
}
