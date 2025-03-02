package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "My CD", description = "사용자의 MyCd 관리 API - CD 추가, 조회, 삭제")
@RestController
@RequestMapping("/api/my-cd")
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;

  @Operation(summary = "CD 추가", description = "사용자의 MyCd 목록에 새로운 CD를 추가합니다.")
  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @AuthenticatedUser Long userId,
      @RequestBody @Valid MyCdCreateRequest myCdRequest
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(myCdService.addCdToMyList(userId, myCdRequest));
  }

  @Operation(summary = "CD 목록 조회", description = "특정 사용자의 MyCd 목록을 조회합니다. 키워드 검색을 지원합니다.")
  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @AuthenticatedUser Long userId, // 로그인한 사용자 ID
      @RequestParam(value = "targetUserId", required = false) Long targetUserId, // 조회 대상 사용자 ID
      @Parameter(description = "커서 기반 페이지네이션 (마지막 조회한 myCdId)")
      @RequestParam(value = "cursor", required = false) Long cursor,
      @Parameter(description = "한 번에 가져올 개수 (기본값: 15)", example = "15")
      @RequestParam(value = "size", defaultValue = "15") int size,
      @Parameter(description = "CD 제목 또는 가수명으로 검색")
      @RequestParam(value = "keyword", required = false) String keyword
  ) {
    Long finalUserId = (targetUserId != null) ? targetUserId : userId; // targetUserId가 있으면 해당 유저 조회

    log.info("요청한 사용자 ID: {}, 조회 대상 사용자 ID: {}", userId, finalUserId);

    return ResponseEntity.ok(myCdService.getMyCdList(finalUserId, keyword, cursor, size));
  }

  @Operation(summary = "특정 CD 조회", description = "사용자의 MyCd 목록에서 특정 CD를 조회합니다.")
  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @AuthenticatedUser Long userId,
      @RequestParam(value = "targetUserId", required = false) Long targetUserId, // 조회 대상 사용자 ID
      @Parameter(description = "조회할 MyCd ID", example = "1")
      @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok(myCdService.getMyCd(userId, myCdId, targetUserId));
  }

  @Operation(summary = "CD 삭제", description = "사용자의 MyCd 목록에서 하나 이상의 CD를 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<Void> delete(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 MyCd ID 목록", example = "[1, 2, 3]")
      @RequestParam List<Long> myCdIds
  ) {
    myCdService.delete(userId, myCdIds);
    return ResponseEntity.noContent().build();
  }
}
