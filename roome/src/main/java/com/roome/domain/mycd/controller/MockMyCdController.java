package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Mock - MyCd", description = "CD 등록/조회/삭제")
@RestController
@RequestMapping("/mock/my-cd")
public class MockMyCdController {

  private static final String DEFAULT_COVER_URL = "https://i.scdn.co/image/ab67616d0000b273a49a8bb234f8741c80d8ec5a";

  @Operation(summary = "Mock - CD 추가", description = "사용자의 MyCd 목록에 CD를 추가")
  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @RequestBody MyCdCreateRequest request) {
    MyCdResponse mockCd = new MyCdResponse(
        1L, 100L, request.getTitle(), request.getArtist(), request.getAlbum(),
        request.getReleaseDate(), request.getGenres(), DEFAULT_COVER_URL,
        request.getYoutubeUrl(), request.getDuration()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(mockCd);
  }

  @Operation(summary = "Mock - CD 목록 조회", description = "사용자의 MyCd 목록을 조회 (무한 스크롤 지원)")
  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "커서 기반 페이지네이션 (마지막 조회한 myCdId)")
      @RequestParam(value = "cursor", required = false) Long cursor,
      @Parameter(description = "한 번에 가져올 개수 (기본값: 15)")
      @RequestParam(value = "size", defaultValue = "15") int size) {

    // 더미 데이터 30개 생성
    List<MyCdResponse> mockData = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      mockData.add(new MyCdResponse(
          (long) i, (long) i, "Song " + i, "Artist " + i, "Album " + i,
          LocalDate.of(2020, (i % 12) + 1, (i % 28) + 1),
          List.of("Genre " + (i % 5 + 1)), DEFAULT_COVER_URL,
          "https://youtube.com/watch?v=video" + i, 180000 + (i * 1000)
      ));
    }

    // 커서 기반 페이지네이션 적용
    List<MyCdResponse> filteredData;
    if (cursor == null || cursor == 0) {
      // 첫 페이지 요청 (cursor 없음 또는 0일 경우)
      filteredData = mockData.stream().limit(size).toList();
    } else {
      // cursor 이후 데이터 중 size 개수만큼 필터링
      filteredData = mockData.stream()
          .filter(cd -> cd.getMyCdId() > cursor) // cursor보다 큰 ID만 선택
          .limit(size) // size만큼 제한
          .toList();
    }

    // nextCursor 설정 (마지막 요소의 ID)
    Long nextCursor = filteredData.isEmpty() ? null : filteredData.get(filteredData.size() - 1).getMyCdId();

    return ResponseEntity.ok(new MyCdListResponse(filteredData, nextCursor));
  }

  @Operation(summary = "Mock - 특정 CD 조회", description = "사용자의 특정 CD 정보를 조회")
  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "조회할 CD의 ID") @PathVariable Long myCdId) {

    MyCdResponse mockCd = new MyCdResponse(
        myCdId, 1L, "Love Poem", "IU", "Love Poem",
        LocalDate.of(2019, 11, 1),
        List.of("Ballad", "Pop"), DEFAULT_COVER_URL,
        "https://youtube.com/watch?v=mnop9876", 240000
    );

    return ResponseEntity.ok(mockCd);
  }

  @Operation(summary = "Mock - 다중 CD 삭제", description = "여러 개의 myCdId를 받아서 삭제")
  @DeleteMapping
  public ResponseEntity<String> deleteMyCds(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "삭제할 CD ID 목록 (예: 1,2,3)") @RequestParam List<Long> myCdIds) {
    return ResponseEntity.ok("userId=" + userId + " 삭제된 CD ID 목록: " + myCdIds);
  }

  @Operation(summary = "Mock - 단일 CD 삭제", description = "특정 myCdId의 CD를 삭제")
  @DeleteMapping("/{myCdId}")
  public ResponseEntity<String> deleteMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "삭제할 CD의 ID") @PathVariable Long myCdId) {
    return ResponseEntity.ok("userId=" + userId + " 삭제된 CD ID: " + myCdId);
  }
}
