package com.roome.domain.user.controller;

import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.service.UserProfileService;
import com.roome.domain.user.temp.UserPrincipal;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "사용자 프로필 관리를 위한 API")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "프로필 조회",
            description = "특정 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 (USER_NOT_FOUND)")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "조회할 사용자의 ID", example = "1")
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authUser) {
        UserProfileResponse response = userProfileService.getUserProfile(userId, authUser.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 수정",
            description = "로그인한 사용자의 프로필 정보(닉네임, 자기소개)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식 또는 유효하지 않은 데이터"),
            @ApiResponse(responseCode = "400", description = "닉네임 형식이 올바르지 않음 (INVALID_NICKNAME_FORMAT)"),
            @ApiResponse(responseCode = "400", description = "자기소개가 비어 있음 (INVALID_BIO_NULL)"),
            @ApiResponse(responseCode = "400", description = "자기소개가 100자를 초과함 (INVALID_BIO_LENGTH)")
    })
    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "수정할 프로필 정보")
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal authUser) {
        validateNickname(request.getNickname());
        validateBio(request.getBio());
        log.info("[프로필 수정] nickname: {}, bio: {}", request.getNickname(), request.getBio());
        UserProfileResponse response = userProfileService.updateProfile(authUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    // 닉네임 유효성 검증
    private void validateNickname(String nickname) {
        if (!nickname.matches("^[가-힣a-zA-Z0-9]{2,20}$")) {
            throw new BusinessException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }
    }

    // 자기소개 유효성 검증
    private void validateBio(String bio) {
        if (bio == null)
        {
            throw new BusinessException(ErrorCode.INVALID_BIO_NULL);
        }
        if (bio.length() > 100) {
            throw new BusinessException(ErrorCode.INVALID_BIO_LENGTH);
        }
    }
}