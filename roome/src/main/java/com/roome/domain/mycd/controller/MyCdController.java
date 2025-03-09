package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "CD 컬렉션 API", description = "사용자의 CD 보관함 관리 API - CD 추가, 조회, 삭제")
@RestController
@RequestMapping("/api/my-cd")
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;

  @Operation(summary = "CD 추가", description = "사용자의 CD 컬렉션에 새로운 CD를 추가합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "CD 추가 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)")
  })
  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @AuthenticatedUser Long userId,
      @RequestBody @Valid MyCdCreateRequest myCdRequest
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(myCdService.addCdToMyList(userId, myCdRequest));
  }

  @Operation(summary = "CD 목록 조회", description = "특정 사용자의 CD 보관함을 조회합니다. 키워드 검색을 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "CD 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음 (USER_NOT_FOUND)")
  })
  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @AuthenticatedUser Long userId, // 로그인한 사용자 ID
      @RequestParam(value = "targetUserId", required = false) Long targetUserId, // 조회 대상 사용자 ID
      @Parameter(description = "커서 기반 페이지네이션 (마지막 조회한 CD ID)")
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

  @Operation(summary = "CD 상세 조회", description = "CD 컬렉션에서 특정 CD의 상세 정보를 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "CD 상세 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "404", description = "CD를 찾을 수 없음 (CD_NOT_FOUND)")
  })
  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @AuthenticatedUser Long userId,
      @RequestParam(value = "targetUserId") Long targetUserId, // targetUserId를 필수로 받도록 변경
      @Parameter(description = "조회할 MyCd ID", example = "1")
      @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok(myCdService.getMyCd(targetUserId, myCdId));
  }

  @Operation(summary = "CD 삭제", description = "CD 컬렉션에서 하나 이상의 CD를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "CD 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (INVALID_REQUEST)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)"),
      @ApiResponse(responseCode = "404", description = "CD를 찾을 수 없음 (CD_NOT_FOUND)")
  })
  @DeleteMapping
  public ResponseEntity<Void> delete(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 CD ID 목록", example = "[1, 2, 3]")
      @RequestParam List<Long> myCdIds
  ) {
    myCdService.delete(userId, myCdIds);
    return ResponseEntity.noContent().build();
  }
}
