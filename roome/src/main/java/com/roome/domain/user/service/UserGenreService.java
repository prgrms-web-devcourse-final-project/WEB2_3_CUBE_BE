package com.roome.domain.user.service;

import com.roome.domain.user.entity.*;
import com.roome.domain.user.repository.UserBookGenreRepository;
import com.roome.domain.user.repository.UserMusicGenreRepository;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGenreService {

    // 사용자 장르 최대 등록 개수
    private static final int MAX_GENRE_COUNT = 3;
    // 추천 사용자 수
    private static final int RECOMMEND_USER_COUNT = 5;
    // 사용자 레포지토리
    private final UserRepository userRepository;
    // 사용자 음악 장르 레포지토리
    private final UserMusicGenreRepository musicGenreRepository;
    // 사용자 책 장르 레포지토리
    private final UserBookGenreRepository bookGenreRepository;

    // 사용자 음악 장르 조회
    public List<MusicGenre> getUserMusicGenres(Long userId) {
        return musicGenreRepository.findByUserId(userId)
                                   .stream()
                                   .map(UserMusicGenre::getGenre)
                                   .collect(Collectors.toList());
    }

    // 사용자 책 장르 조회
    public List<BookGenre> getUserBookGenres(Long userId) {
        return bookGenreRepository.findByUserId(userId)
                                  .stream()
                                  .map(UserBookGenre::getGenre)
                                  .collect(Collectors.toList());
    }

    // 음악 장르 추가
    @Transactional
    public void addMusicGenre(Long userId, MusicGenre genre) {
        validateGenreCount(musicGenreRepository.countByUserId(userId));
        validateDuplicateGenre(userId, genre);

        UserMusicGenre userMusicGenre = UserMusicGenre.create(userId, genre);
        musicGenreRepository.save(userMusicGenre);
    }

    // 책 장르 추가
    @Transactional
    public void addBookGenre(Long userId, BookGenre genre) {
        validateGenreCount(bookGenreRepository.countByUserId(userId));
        validateDuplicateGenre(userId, genre);

        UserBookGenre userBookGenre = UserBookGenre.create(userId, genre);
        bookGenreRepository.save(userBookGenre);
    }

    // 음악 장르 삭제
    @Transactional
    public void updateMusicGenres(Long userId, List<MusicGenre> genres) {
        validateGenreListSize(genres);
        musicGenreRepository.deleteByUserId(userId);

        genres.forEach(genre -> {
            UserMusicGenre userMusicGenre = UserMusicGenre.create(userId, genre);
            musicGenreRepository.save(userMusicGenre);
        });
    }

    // 책 장르 삭제
    @Transactional
    public void updateBookGenres(Long userId, List<BookGenre> genres) {
        validateGenreListSize(genres);
        bookGenreRepository.deleteByUserId(userId);

        genres.forEach(genre -> {
            UserBookGenre userBookGenre = UserBookGenre.create(userId, genre);
            bookGenreRepository.save(userBookGenre);
        });
    }

    // 추천 사용자 조회
    public List<User> getRecommendedUsers(Long userId) {
        // 현재 사용자의 장르 가져오기
        List<MusicGenre> userMusicGenres = getUserMusicGenres(userId);
        List<BookGenre> userBookGenres = getUserBookGenres(userId);

        // 모든 사용자 가져오기 (현재 사용자 제외)
        List<User> allUsers = userRepository.findAll().stream()
                                            .filter(user -> !user.getId().equals(userId))
                                            .collect(Collectors.toList());

        // 각 사용자별 매칭 점수 계산
        return allUsers.stream()
                       .map(user -> {
                           int matchScore = calculateMatchScore(
                                   user.getId(),
                                   userMusicGenres,
                                   userBookGenres
                                                               );
                           return new AbstractMap.SimpleEntry<>(user, matchScore);
                       })
                       .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                       .limit(RECOMMEND_USER_COUNT)
                       .map(Map.Entry::getKey)
                       .collect(Collectors.toList());
    }

    // 매칭 점수 계산
    private int calculateMatchScore(Long targetUserId,
                                    List<MusicGenre> userMusicGenres,
                                    List<BookGenre> userBookGenres) {
        List<MusicGenre> targetMusicGenres = getUserMusicGenres(targetUserId);
        List<BookGenre> targetBookGenres = getUserBookGenres(targetUserId);

        int musicMatchCount = (int) userMusicGenres.stream()
                                                   .filter(targetMusicGenres::contains)
                                                   .count();

        int bookMatchCount = (int) userBookGenres.stream()
                                                 .filter(targetBookGenres::contains)
                                                 .count();

        return musicMatchCount + bookMatchCount;
    }

    // 사용자 음악 장르 삭제
    private void validateGenreCount(long currentCount) {
        if (currentCount >= MAX_GENRE_COUNT) {
            throw new BusinessException(ErrorCode.GENRE_LIMIT_EXCEEDED);
        }
    }

    // 사용자 책 장르 삭제
    private void validateGenreListSize(List<?> genres) {
        if (genres.size() > MAX_GENRE_COUNT) {
            throw new BusinessException(ErrorCode.GENRE_LIMIT_EXCEEDED);
        }
    }

    // 중복 장르 검증
    private void validateDuplicateGenre(Long userId, MusicGenre genre) {
        if (musicGenreRepository.existsByUserIdAndGenre(userId, genre)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GENRE);
        }
    }

    // 중복 장르 검증
    private void validateDuplicateGenre(Long userId, BookGenre genre) {
        if (bookGenreRepository.existsByUserIdAndGenre(userId, genre)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GENRE);
        }
    }
}