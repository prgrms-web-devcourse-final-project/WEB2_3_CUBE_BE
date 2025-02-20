package com.roome.domain.user.controller;

import com.roome.domain.user.dto.request.MockUpdateProfileRequest;
import com.roome.domain.user.dto.response.MockUserProfileResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mock/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 조회 및 수정")
public class MockUserController {

    @GetMapping("/{userId}")
    public ResponseEntity<MockUserProfileResponse> getMockUserProfile(@PathVariable Long userId) {
        log.info("[Mock 프로필 조회] 사용자 ID: {}", userId);

        MockUserProfileResponse mockResponse = MockUserProfileResponse.builder()
                .id(userId.toString())
                .nickname("Mock User " + userId)
                .profileImage("https://mock-image.com/user" + userId + ".png")
                .bio("안녕하세요. Mock입니다..")
                .bookGenres(List.of("Fantasy", "Mystery", "Sci-Fi"))
                .musicGenres(List.of("Pop", "Jazz", "Rock"))
                .similarUser(List.of(
                        new MockUserProfileResponse.SimilarUser("101", "Mock Friend 1", "https://mock-image.com/friend1.png"),
                        new MockUserProfileResponse.SimilarUser("102", "Mock Friend 2", "https://mock-image.com/friend2.png")
                ))
                .build();

        return ResponseEntity.ok(mockResponse);
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateMockUserProfile(@RequestBody MockUpdateProfileRequest request) {
        log.info("[Mock 프로필 수정] nickname: {}, profileImage: {}, bio: {}",
                request.getNickname(), request.getProfileImage(), request.getBio());

        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "error",
                    "message", "잘못된 프로필 정보입니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "id", "999",
                "nickname", request.getNickname(),
                "profileImage", request.getProfileImage(),
                "bio", request.getBio(),
                "message", "프로필이 수정되었습니다"
        ));
    }
}
