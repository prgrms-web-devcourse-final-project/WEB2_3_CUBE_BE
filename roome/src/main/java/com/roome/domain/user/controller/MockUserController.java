package com.roome.domain.user.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.dto.RecommendedUserDto;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.ImageUploadResponseDto;
import com.roome.domain.user.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/mock/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 조회 및 수정")
public class MockUserController {

    private static final Long MOCK_USER_ID = 999L;

    @Operation(summary = "프로필 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getMockUserProfile(@PathVariable Long userId) {
        log.info("[Mock 프로필 조회] 사용자 ID: {}", userId);

        UserProfileResponse mockResponse = UserProfileResponse
                .builder()
                .id(userId.toString())
                .nickname("Mock User " + userId)
                .profileImage("https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
                .bio("안녕하세요. Mock입니다..")
                .bookGenres(List.of("NOVEL", "ESSAY"))
                .musicGenres(List.of("POP", "ROCK"))
                .recommendedUsers(generateMockSimilarUsers())
                .isMyProfile(true)
                .build();

        return ResponseEntity.ok(mockResponse);
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/profile")
    public ResponseEntity<?> updateMockUserProfile(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestBody UpdateProfileRequest request) {
        log.info("[Mock 프로필 수정] nickname: {}, bio: {}", request.getNickname(), request.getBio());

        return ResponseEntity.ok(
                Map.of("id", MOCK_USER_ID.toString(), "nickname", request.getNickname(), "bio", request.getBio()));
    }

    @Operation(summary = "프로필 이미지 업로드")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ImageUploadResponseDto> uploadMockProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("image") MultipartFile image) {
        log.info("[Mock 프로필 이미지 업로드] 파일명: {}, 크기: {}", image.getOriginalFilename(), image.getSize());

        // 모의 S3 URL 생성 (실제 업로드는 이루어지지 않음)
        String mockUuid = UUID
                .randomUUID()
                .toString()
                .replace("-", "");
        String mockFileExtension = "jpg";
        String mockImageUrl = "https://roome-profile-images.s3.ap-northeast-2.amazonaws.com/profile/" + mockUuid + "." + mockFileExtension;

        ImageUploadResponseDto response = ImageUploadResponseDto
                .builder()
                .imageUrl(mockImageUrl)
                .fileName(image.getOriginalFilename())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 이미지 삭제")
    @DeleteMapping("/profile/image")
    public ResponseEntity<?> deleteMockProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("imageUrl") String imageUrl) {
        log.info("[Mock 프로필 이미지 삭제] URL: {}", imageUrl);


        return ResponseEntity.ok(imageUrl);
    }

    private List<RecommendedUserDto> generateMockSimilarUsers() {
        return List.of(
                RecommendedUserDto.builder().userId(1L).nickname("Mock User 1").profileImage("""
                        https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813""").build(),
                RecommendedUserDto.builder().userId(2L).nickname("Mock User 2").profileImage("""
                        https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813""").build(),
                RecommendedUserDto.builder().userId(3L).nickname("Mock User 3").profileImage("""
                        https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813""").build(),
                RecommendedUserDto.builder().userId(3L).nickname("Mock User 4").profileImage("""
                        https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813""").build(),
                RecommendedUserDto.builder().userId(3L).nickname("Mock User 5").profileImage("""
                        https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813""").build()

        );
    }
}