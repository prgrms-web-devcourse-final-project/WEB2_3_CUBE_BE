package com.roome.domain.user.controller;

import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.service.UserProfileService;
import com.roome.domain.user.temp.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        UserProfileResponse response = userProfileService.updateProfile(authUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}