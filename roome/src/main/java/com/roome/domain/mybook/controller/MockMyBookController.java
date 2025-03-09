package com.roome.domain.mybook.controller;

import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/mock/mybooks")
@Tag(name = "MyBook", description = "도서 등록/조회/삭제")
public class MockMyBookController {

  @Tag(name = "도서 등록", description = "도서를 등록할 수 있다.")
  @PostMapping()
  public ResponseEntity<MyBookResponse> create(
      @RequestParam("userId") Long userId,
      @RequestBody MyBookCreateRequest request
  ) {
    MyBookResponse response = createMyBookResponse(1L, "title");
    return ResponseEntity.ok(response);
  }

  @Tag(name = "등록 도서 상세 조회", description = "등록된 도서 목록에서 도서를 클릭하면 도서의 상세 정보를 조회할 수 있다.")
  @GetMapping("/{myBookId}")
  public ResponseEntity<MyBookResponse> read(@PathVariable Long myBookId) {
    MyBookResponse response = createMyBookResponse(1L, "title");
    return ResponseEntity.ok(response);
  }

  @Tag(name = "등록 도서 목록 조회", description = "등록된 도서 목록을 조회할 수 있다. 무한 스크롤 방식.")
  @GetMapping()
  public ResponseEntity<MyBooksResponse> readAll(
      @RequestParam("userId") Long userId,
      @RequestParam("pageSize") Long pageSize,
      @RequestParam(value = "lastMyBookId", required = false) Long lastMyBookId
  ) {
    MyBooksResponse response = createMyBooksResponse(
        List.of(
            createMyBookResponse(5L, "title5"),
            createMyBookResponse(4L, "title4")
        ),
        5L
    );
    return ResponseEntity.ok(response);
  }

  @Tag(name = "등록 도서 삭제", description = "등록된 도서를 삭제할 수 있다.")
  @DeleteMapping()
  public ResponseEntity<Void> delete(
      @RequestParam("userId") Long userId,
      @RequestParam String myBookIds
  ) {
    return ResponseEntity.ok().build();
  }

  private MyBookResponse createMyBookResponse(Long myBookId, String title) {
    return new MyBookResponse(
        myBookId,
        title,
        "author",
        "publisher",
        LocalDate.of(2025, 1, 1),
        "image.url",
        List.of("웹", "IT"),
        321L
    );
  }

  private MyBooksResponse createMyBooksResponse(List<MyBookResponse> myBookResponses,
      Long totalMyBookCount) {
    return new MyBooksResponse(
        myBookResponses,
        totalMyBookCount
    );
  }
}
