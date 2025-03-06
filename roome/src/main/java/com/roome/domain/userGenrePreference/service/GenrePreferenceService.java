package com.roome.domain.userGenrePreference.service;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.userGenrePreference.entity.GenreType;
import com.roome.domain.userGenrePreference.entity.UserGenrePreference;
import com.roome.domain.userGenrePreference.repository.UserGenrePreferenceRepository;
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
public class GenrePreferenceService {

    private final UserRepository userRepository;
    private final MyCdRepository myCdRepository;
    private final MyBookRepository myBookRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;

    // 사용자의 CD 장르 선호도 조회
    public List<String> getTopCdGenres(Long userId) {
        List<MyCd> userCds = myCdRepository.findByUserId(userId);

        if (userCds.isEmpty()) {
            return Collections.emptyList();
        }

        // 장르 카운팅
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MyCd myCd : userCds) {
            List<String> genres = myCd.getCd().getGenres();
            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
            }
        }

        // 상위 3개 장르 반환
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 사용자의 책 장르 선호도 조회
    public List<String> getTopBookGenres(Long userId) {
        List<MyBook> userBooks = myBookRepository.findAllByUserId(userId);

        if (userBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // 장르 카운팅
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MyBook myBook : userBooks) {
            List<String> genres = myBook.getBook().getBookGenres().stream()
                    .map(bg -> bg.getGenre().getName())
                    .collect(Collectors.toList());

            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
            }
        }

        // 상위 3개 장르 반환
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // CD 장르 선호도 저장 (캐싱 목적)
    @Transactional
    public void updateCdGenrePreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MyCd> userCds = myCdRepository.findByUserId(userId);

        // 기존 선호도 삭제
        userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.CD);

        if (userCds.isEmpty()) {
            return;
        }

        // 장르 카운팅
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MyCd myCd : userCds) {
            List<String> genres = myCd.getCd().getGenres();
            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
            }
        }

        // 상위 3개 장르 저장
        genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    UserGenrePreference preference = UserGenrePreference.create(
                            user, GenreType.CD, entry.getKey()
                    );
                    userGenrePreferenceRepository.save(preference);
                });
    }

    // 책 장르 선호도 저장 (캐싱 목적)
    @Transactional
    public void updateBookGenrePreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MyBook> userBooks = myBookRepository.findAllByUserId(userId);

        // 기존 선호도 삭제
        userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.BOOK);

        if (userBooks.isEmpty()) {
            return;
        }

        // 장르 카운팅
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MyBook myBook : userBooks) {
            List<String> genres = myBook.getBook().getBookGenres().stream()
                    .map(bg -> bg.getGenre().getName())
                    .collect(Collectors.toList());

            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
            }
        }

        // 상위 3개 장르 저장
        genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    UserGenrePreference preference = UserGenrePreference.create(
                            user, GenreType.BOOK, entry.getKey()
                    );
                    userGenrePreferenceRepository.save(preference);
                });
    }
}