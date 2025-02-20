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
import java.util.stream.Collectors;

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
                .similarUser(generateMockSimilarUsers())
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

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<MockUserProfileResponse.SimilarUser>> getRecommendedUsers(@PathVariable Long userId) {
        log.info("[Mock 추천 유저 조회] 사용자 ID: {}", userId);

        // 취향이 4개 이상 겹치는 유저 5명 추천
        List<MockUserProfileResponse.SimilarUser> recommendedUsers = generateMockSimilarUsers().stream()
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommendedUsers);
    }

    private List<MockUserProfileResponse.SimilarUser> generateMockSimilarUsers() {
        return List.of(
                new MockUserProfileResponse.SimilarUser("101", "Similar User 1", "https://mock-image.com/similar1.png"),
                new MockUserProfileResponse.SimilarUser("102", "Similar User 2", "https://mock-image.com/similar2.png"),
                new MockUserProfileResponse.SimilarUser("103", "Similar User 3", "https://mock-image.com/similar3.png"),
                new MockUserProfileResponse.SimilarUser("104", "Similar User 4", "https://mock-image.com/similar4.png"),
                new MockUserProfileResponse.SimilarUser("105", "Similar User 5", "https://mock-image.com/similar5.png")
        );
    }
}
