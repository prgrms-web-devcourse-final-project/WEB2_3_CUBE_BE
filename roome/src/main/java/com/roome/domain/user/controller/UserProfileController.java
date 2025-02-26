package com.roome.domain.user.controller;

import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.service.UserProfileService;
import com.roome.domain.user.temp.UserPrincipal;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "프로필 조회")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authUser) {
        UserProfileResponse response = userProfileService.getUserProfile(userId, authUser.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
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