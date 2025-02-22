package com.roome.domain.user.controller;

import com.roome.domain.user.dto.request.MockUpdateProfileRequest;
import com.roome.domain.user.dto.response.MockUserProfileResponse;
import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.MusicGenre;
import com.roome.domain.user.entity.UserBookGenre;
import com.roome.domain.user.entity.UserMusicGenre;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Operation(summary = "프로필 수정")
    @PatchMapping("/profile")
    public ResponseEntity<?> updateMockUserProfile(@RequestBody MockUpdateProfileRequest request) {
        log.info("[Mock 프로필 수정] nickname: {}, profileImage: {}, bio: {}",
                 request.getNickname(), request.getProfileImage(), request.getBio());

        return ResponseEntity.ok(Map.of(
                "id", MOCK_USER_ID.toString(),
                "nickname", request.getNickname(),
                "profileImage", request.getProfileImage(),
                "bio", request.getBio()
                                       ));
    }

    @Operation(summary = "음악 감성 목록 조회")
    @GetMapping("/music-genres")
    public ResponseEntity<List<String>> getMusicGenres() {
        List<String> genres = Arrays.stream(MusicGenre.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "음악 감성 추가")
    @PostMapping("/music-genres")
    public ResponseEntity<UserMusicGenre> addMusicGenre(@RequestBody String genre) {
        log.info("[Mock 음악 감성 추가] genre: {}", genre);

        UserMusicGenre addedGenre = UserMusicGenre.create(MOCK_USER_ID, MusicGenre.valueOf(genre));
        return ResponseEntity.ok(addedGenre);
    }

    @Operation(summary = "음악 감성 수정")
    @PutMapping("/music-genres")
    public ResponseEntity<List<UserMusicGenre>> updateMusicGenres(@RequestBody List<String> genres) {
        log.info("[Mock 음악 감성 수정] genres: {}", genres);

        List<UserMusicGenre> updatedGenres = genres.stream()
                                                   .map(genre -> UserMusicGenre.create(MOCK_USER_ID, MusicGenre.valueOf(genre)))
                                                   .collect(Collectors.toList());

        return ResponseEntity.ok(updatedGenres);
    }

    @Operation(summary = "독서 취향 목록 조회")
    @GetMapping("/book-genres")
    public ResponseEntity<List<String>> getBookGenres() {
        List<String> genres = Arrays.stream(BookGenre.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "독서 취향 추가")
    @PostMapping("/book-genres")
    public ResponseEntity<UserBookGenre> addBookGenre(@RequestBody String genre) {
        log.info("[Mock 독서 취향 추가] genre: {}", genre);

        UserBookGenre addedGenre = UserBookGenre.create(MOCK_USER_ID, BookGenre.valueOf(genre));
        return ResponseEntity.ok(addedGenre);
    }

    @Operation(summary = "독서 취향 수정")
    @PutMapping("/book-genres")
    public ResponseEntity<List<UserBookGenre>> updateBookGenres(@RequestBody List<String> genres) {
        log.info("[Mock 독서 취향 수정] genres: {}", genres);

        List<UserBookGenre> updatedGenres = genres.stream()
                                                  .map(genre -> UserBookGenre.create(MOCK_USER_ID, BookGenre.valueOf(genre)))
                                                  .collect(Collectors.toList());

        return ResponseEntity.ok(updatedGenres);
    }

    @Operation(summary = "추천 유저 조회")
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<MockUserProfileResponse.SimilarUser>> getRecommendedUsers(@PathVariable Long userId) {
        log.info("[Mock 추천 유저 조회] 사용자 ID: {}", userId);

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