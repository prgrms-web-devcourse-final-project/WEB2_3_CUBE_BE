package com.roome.domain.user.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.dto.request.MockUpdateProfileRequest;
import com.roome.domain.user.dto.response.ImageDeleteResponseDto;
import com.roome.domain.user.dto.response.ImageUploadResponseDto;
import com.roome.domain.user.dto.response.MockUserProfileResponse;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.MusicGenre;
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
import java.util.stream.Collectors;

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
                .bookGenres(List.of(BookGenre.SF, BookGenre.LIFE_SCIENCE))
                .musicGenres(List.of(MusicGenre.AFRO, MusicGenre.HIPHOP))
                .isMyProfile(true)
                .build();

        return ResponseEntity.ok(mockResponse);
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/profile")
    public ResponseEntity<?> updateMockUserProfile(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestBody MockUpdateProfileRequest request) {
        log.info("[Mock 프로필 수정] nickname: {}, profileImage: {}, bio: {}", request.getNickname(), request.getBio());

        return ResponseEntity.ok(
                Map.of("id", MOCK_USER_ID.toString(), "nickname", request.getNickname(), "bio", request.getBio()));
    }

    @Operation(summary = "프로필 이미지 업로드")
    @PostMapping("/profile/image")
    public ResponseEntity<ImageUploadResponseDto> uploadMockProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("image") MultipartFile image) {
        log.info("[Mock 프로필 이미지 업로드] 파일명: {}, 크기: {}", image.getOriginalFilename(), image.getSize());

        // 모의 S3 URL 생성 (실제 업로드는 이루어지지 않음)
        String mockUuid = UUID
                .randomUUID()
                .toString()
                .replace("-", "");
        String mockFileExtension = getFileExtension(image.getOriginalFilename());
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
    public ResponseEntity<ImageDeleteResponseDto> deleteMockProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("imageUrl") String imageUrl) {
        log.info("[Mock 프로필 이미지 삭제] URL: {}", imageUrl);

        // 실제 삭제 로직 없이 성공 응답 반환
        ImageDeleteResponseDto response = ImageDeleteResponseDto
                .builder()
                .imageUrl(imageUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "추천 유저 조회")
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<MockUserProfileResponse.SimilarUser>> getRecommendedUsers(@AuthenticationPrincipal OAuth2UserPrincipal principal, @PathVariable Long userId) {
        log.info("[Mock 추천 유저 조회] 사용자 ID: {}", userId);

        List<MockUserProfileResponse.SimilarUser> recommendedUsers = generateMockSimilarUsers()
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommendedUsers);
    }

    private List<MockUserProfileResponse.SimilarUser> generateMockSimilarUsers() {
        return List.of(new MockUserProfileResponse.SimilarUser("101", "Similar User 1",
                                                               "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813"),
                       new MockUserProfileResponse.SimilarUser("102", "Similar User 2",
                                                               "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813"),
                       new MockUserProfileResponse.SimilarUser("103", "Similar User 3",
                                                               "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813"),
                       new MockUserProfileResponse.SimilarUser("104", "Similar User 4",
                                                               "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813"),
                       new MockUserProfileResponse.SimilarUser("105", "Similar User 5",
                                                               "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813"));
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "jpg";
    }
}