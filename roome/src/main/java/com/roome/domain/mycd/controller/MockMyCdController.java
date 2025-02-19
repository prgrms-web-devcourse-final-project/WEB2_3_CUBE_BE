package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mock - MyCd API", description = "MyCd 관련 Mock API")
@RestController
@RequestMapping("/mock/mycd")
public class MockMyCdController {

  @Operation(summary = "Mock - 내 CD 목록 조회", description = "userId가 없으면 본인 목록, 있으면 해당 userId의 CD 목록 반환")
  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(@RequestParam(value = "userId", required = false) Long userId) {

    List<MyCdResponse> mockData = List.of(
        new MyCdResponse(1L, 1L, "Palette", "IU", "Palette", List.of("K-Pop", "Ballad"),
            "https://example.com/image1.jpg", "https://youtube.com/watch?v=asdf5678", 215000),
        new MyCdResponse(2L, 2L, "The Red Shoes", "IU", "Modern Times", List.of("Jazz"),
            "https://example.com/image2.jpg", "https://youtube.com/watch?v=zxcv1234", 245000),
        new MyCdResponse(3L, 3L, "Good Day", "IU", "Good Day", List.of("Pop"),
            "https://example.com/image3.jpg", "https://youtube.com/watch?v=qazx5678", 230000),
        new MyCdResponse(4L, 4L, "BBIBBI", "IU", "BBIBBI", List.of("K-Pop"),
            "https://example.com/image4.jpg", "https://youtube.com/watch?v=wsxedc90", 210000),
        new MyCdResponse(5L, 5L, "You and I", "IU", "Last Fantasy", List.of("Ballad"),
            "https://example.com/image5.jpg", "https://youtube.com/watch?v=poiuyt12", 250000)
    );

    return ResponseEntity.ok(new MyCdListResponse(mockData));
  }

  @Operation(summary = "Mock - 특정 CD 조회", description = "특정 myCdId의 상세 정보 반환")
  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(@PathVariable Long myCdId) {

    MyCdResponse mockCd = new MyCdResponse(
        myCdId, 1L, "Love Poem", "IU", "Love Poem", List.of("Ballad", "Pop"),
        "https://example.com/image6.jpg", "https://youtube.com/watch?v=mnop9876", 240000);

    return ResponseEntity.ok(mockCd);
  }

  @Operation(summary = "Mock - 내 CD 추가", description = "사용자의 MyCd 목록에 CD를 추가")
  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(@RequestBody MyCdCreateRequest request) {

    MyCdResponse mockCd = new MyCdResponse(
        1L,
        request.getCdId(),
        request.getTitle(),
        request.getArtist(),
        request.getAlbum(),
        List.of(request.getGenre()),
        request.getCoverUrl(),
        request.getYoutubeUrl(),
        request.getDuration()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(mockCd);
  }

  @Operation(summary = "Mock - 내 CD 다중 삭제", description = "여러 개의 myCdId를 받아서 삭제 (Mock)")
  @DeleteMapping
  public ResponseEntity<String> deleteMyCds(@RequestParam("myCdIds") String myCdIds) {
    List<Long> deletedIds = Arrays.stream(myCdIds.split(","))
        .map(Long::parseLong)
        .collect(Collectors.toList());

    return ResponseEntity.ok("삭제된 CD ID 목록: " + deletedIds);
  }

  @Operation(summary = "Mock - 특정 CD 삭제", description = "특정 myCdId의 CD를 삭제")
  @DeleteMapping("/{myCdId}")
  public ResponseEntity<String> deleteMyCd(@PathVariable Long myCdId) {
    return ResponseEntity.ok("삭제된 CD ID: " + myCdId);
  }

}
