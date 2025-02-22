package com.roome.domain.user.controller;

import com.roome.domain.user.entity.*;
import com.roome.domain.user.service.UserGenreService;
import com.roome.domain.user.temp.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Genre", description = "장르 API")
public class UserGenreController {

    private final UserGenreService userGenreService;

    @Operation(summary = "음악 감성 목록 조회")
    @GetMapping("/music-genres")
    public ResponseEntity<List<String>> getMusicGenres() {
        List<String> genres = Arrays.stream(MusicGenre.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "사용자의 음악 감성 조회")
    @GetMapping("/{userId}/music-genres")
    public ResponseEntity<List<MusicGenre>> getUserMusicGenres(@PathVariable Long userId) {
        return ResponseEntity.ok(userGenreService.getUserMusicGenres(userId));
    }

    @Operation(summary = "음악 감성 추가")
    @PostMapping("/music-genres")
    public ResponseEntity<Void> addMusicGenre(
            @RequestBody String genre,
            @AuthenticationPrincipal UserPrincipal authUser) {
        userGenreService.addMusicGenre(authUser.getId(), MusicGenre.valueOf(genre));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "음악 감성 수정")
    @PutMapping("/music-genres")
    public ResponseEntity<Void> updateMusicGenres(
            @RequestBody List<String> genres,
            @AuthenticationPrincipal UserPrincipal authUser) {
        List<MusicGenre> musicGenres = genres.stream()
                                             .map(MusicGenre::valueOf)
                                             .collect(Collectors.toList());
        userGenreService.updateMusicGenres(authUser.getId(), musicGenres);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "독서 취향 목록 조회")
    @GetMapping("/book-genres")
    public ResponseEntity<List<String>> getBookGenres() {
        List<String> genres = Arrays.stream(BookGenre.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "사용자의 독서 취향 조회")
    @GetMapping("/{userId}/book-genres")
    public ResponseEntity<List<BookGenre>> getUserBookGenres(@PathVariable Long userId) {
        return ResponseEntity.ok(userGenreService.getUserBookGenres(userId));
    }

    @Operation(summary = "독서 취향 추가")
    @PostMapping("/book-genres")
    public ResponseEntity<Void> addBookGenre(
            @RequestBody String genre,
            @AuthenticationPrincipal UserPrincipal authUser) {
        userGenreService.addBookGenre(authUser.getId(), BookGenre.valueOf(genre));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "독서 취향 수정")
    @PutMapping("/book-genres")
    public ResponseEntity<Void> updateBookGenres(
            @RequestBody List<String> genres,
            @AuthenticationPrincipal UserPrincipal authUser) {
        List<BookGenre> bookGenres = genres.stream()
                                           .map(BookGenre::valueOf)
                                           .collect(Collectors.toList());
        userGenreService.updateBookGenres(authUser.getId(), bookGenres);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "추천 유저 조회")
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<User>> getRecommendedUsers(@PathVariable Long userId) {
        return ResponseEntity.ok(userGenreService.getRecommendedUsers(userId));
    }
}