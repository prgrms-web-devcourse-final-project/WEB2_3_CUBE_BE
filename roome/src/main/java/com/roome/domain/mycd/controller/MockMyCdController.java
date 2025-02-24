package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mock - MyCd", description = "CD 등록/조회/삭제")
@RestController
@RequestMapping("/mock/my-cd")
public class MockMyCdController {

  @Operation(summary = "Mock - CD 추가", description = "사용자의 MyCd 목록에 CD를 추가")
  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @RequestBody MyCdCreateRequest request
  ) {
    MyCdResponse mockCd = new MyCdResponse(
        1L, // 등록된 myCdId (임시 데이터)
        100L, // 새롭게 등록된 CD의 ID (임시 데이터)
        request.getTitle(),
        request.getArtist(),
        request.getAlbum(),
        request.getGenres(),
        request.getCoverUrl(),
        request.getYoutubeUrl(),
        request.getDuration()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(mockCd);
  }

  @Operation(summary = "Mock - CD 목록 조회", description = "사용자의 MyCd 목록을 조회 (무한 스크롤 지원)")
  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "커서 기반 페이지네이션 (마지막 조회한 myCdId)") @RequestParam(value = "cursor", required = false) Long cursor,
      @Parameter(description = "한 번에 가져올 개수 (기본값: 10)") @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    List<MyCdResponse> mockData = List.of(
        new MyCdResponse(1L, 1L, "Palette", "IU", "Palette",
            List.of("K-Pop", "Ballad"), "https://example.com/image1.jpg",
            "https://youtube.com/watch?v=asdf5678", 215000),
        new MyCdResponse(2L, 2L, "The Red Shoes", "IU", "Modern Times",
            List.of("Jazz"), "https://example.com/image2.jpg",
            "https://youtube.com/watch?v=zxcv1234", 245000)
    );

    Long nextCursor = mockData.isEmpty() ? null : mockData.get(mockData.size() - 1).getMyCdId();

    return ResponseEntity.ok(new MyCdListResponse(mockData, nextCursor));
  }

  @Operation(summary = "Mock - 특정 CD 조회", description = "사용자의 특정 CD 정보를 조회")
  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "조회할 CD의 ID") @PathVariable Long myCdId
  ) {
    MyCdResponse mockCd = new MyCdResponse(
        myCdId, 1L, "Love Poem", "IU", "Love Poem",
        List.of("Ballad", "Pop"), "https://example.com/image6.jpg",
        "https://youtube.com/watch?v=mnop9876", 240000
    );

    return ResponseEntity.ok(mockCd);
  }

  @Operation(summary = "Mock - 다중 CD 삭제", description = "여러 개의 myCdId를 받아서 삭제")
  @DeleteMapping
  public ResponseEntity<String> deleteMyCds(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "삭제할 CD ID 목록 (예: 1,2,3)") @RequestParam List<Long> myCdIds
  ) {
    return ResponseEntity.ok("userId=" + userId + " 삭제된 CD ID 목록: " + myCdIds);
  }

  @Operation(summary = "Mock - 단일 CD 삭제", description = "특정 myCdId의 CD를 삭제")
  @DeleteMapping("/{myCdId}")
  public ResponseEntity<String> deleteMyCd(
      @Parameter(description = "사용자 ID", required = true) @RequestParam("userId") Long userId,
      @Parameter(description = "삭제할 CD의 ID") @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok("userId=" + userId + " 삭제된 CD ID: " + myCdId);
  }
}
