package com.roome.domain.houseMate.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.houseMate.service.HousemateService;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Housemate", description = "하우스메이트 관리를 위한 API - 서로의 방을 방문하고 소통할 수 있는 친구 시스템")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates")
public class HousemateController {

    private final HousemateService housemateService;

    @Operation(summary = "나를 추가한 메이트 목록 조회",
            description = "현재 사용자를 하우스메이트로 추가한 사용자들의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메이트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서 형식 (INVALID_CURSOR_VALUE)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 limit 값 (INVALID_LIMIT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/followers")
    public ResponseEntity<HousemateListResponse> getFollowers(
            @AuthenticationPrincipal OAuth2UserPrincipal principal,
            @Parameter(description = "페이지네이션 커서 (마지막으로 받은 userId)", example = "10")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 페이지당 조회할 메이트 수(1-100)", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "닉네임으로 검색", example = "김철수")
            @RequestParam(required = false) String nickname) {

        // 커서 검증
        validateCursor(cursor);
        // 리미티 검증
        validateLimit(limit);

        HousemateListResponse response = housemateService.getFollowerList(principal.getId(), cursor, limit, nickname);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 추가한 메이트 목록 조회",
            description = "현재 사용자가 하우스메이트로 추가한 사용자들의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메이트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서 형식 (INVALID_CURSOR_VALUE)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 limit 값 (INVALID_LIMIT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/following")
    public ResponseEntity<HousemateListResponse> getFollowing(
            @AuthenticationPrincipal OAuth2UserPrincipal principal,
            @Parameter(description = "페이지네이션 커서 (마지막으로 받은 userId)", example = "10")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 페이지당 조회할 메이트 수(1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Max(100) @Min(1) int limit,
            @Parameter(description = "닉네임으로 검색", example = "김철수")
            @RequestParam(required = false) String nickname) {

        // 커서 검증
        validateCursor(cursor);
        // 리미티 검증
        validateLimit(limit);


        HousemateListResponse response = housemateService.getFollowingList(principal.getId(), cursor, limit, nickname);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "하우스메이트 추가",
            description = "특정 사용자를 하우스메이트로 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "하우스메이트 추가 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID"),
            @ApiResponse(responseCode = "400", description = "자기 자신을 하우스메이트로 추가 시도 (SELF_FOLLOW_NOT_ALLOWED)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 (USER_NOT_FOUND)"),
            @ApiResponse(responseCode = "409", description = "이미 추가된 하우스메이트 (ALREADY_HOUSEMATE)")
    })
    @PostMapping("/follow/{targetId}")
    public ResponseEntity<Void> addHousemate(
            @AuthenticationPrincipal OAuth2UserPrincipal principal,
            @Parameter(description = "추가할 사용자의 ID", example = "1")
            @PathVariable Long targetId) {

        validateFollowTarget(principal.getId(), targetId);

        housemateService.addHousemate(principal.getId(), targetId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "하우스메이트 삭제",
            description = "특정 사용자를 하우스메이트에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "하우스메이트 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID"),
            @ApiResponse(responseCode = "400", description = "자기 자신을 대상으로 시도 (SELF_FOLLOW_NOT_ALLOWED)"),
            @ApiResponse(responseCode = "400", description = "하우스메이트로 추가되지 않은 사용자 (NOT_HOUSEMATE)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 (USER_NOT_FOUND)")
    })
    @DeleteMapping("/follow/{targetId}")
    public ResponseEntity<Void> removeHousemate(
            @AuthenticationPrincipal OAuth2UserPrincipal principal,
            @Parameter(description = "삭제할 사용자의 ID", example = "1")
            @PathVariable Long targetId) {

        validateFollowTarget(principal.getId(), targetId);

        housemateService.removeHousemate(principal.getId(), targetId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 팔로우 대상 유효성 검증
    private void validateFollowTarget(Long userId, Long targetId) {
        if (targetId == null || targetId <= 0) {
            log.error("유효하지 않은 targetId: {}", targetId);
            throw new ControllerException(ErrorCode.USER_NOT_FOUND);
        }

        if (userId.equals(targetId)) {
            log.error("자기 자신을 하우스메이트로 추가 시도: userId={}, targetId={}", userId, targetId);
            throw new ControllerException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
    }

    // 리미티 검증
    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            log.error("유효하지 않은 limit 값: {}", limit);
            throw new ControllerException(ErrorCode.INVALID_LIMIT_VALUE);
        }
    }

    //커서 검증
    private void validateCursor(Long cursor) {
        if (cursor != null && cursor <= 0) {
            log.error("유효하지 않은 cursor 값: {}", cursor);
            throw new ControllerException(ErrorCode.INVALID_CURSOR_VALUE);
        }
    }
}