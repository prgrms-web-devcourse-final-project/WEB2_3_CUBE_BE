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
        return userGenrePreferenceRepository
                .findByUserIdAndGenreTypeOrderByRankAsc(userId, GenreType.CD)
                .stream()
                .map(UserGenrePreference::getGenreName)
                .collect(Collectors.toList());
    }

    // 사용자의 책 장르 선호도 조회
    public List<String> getTopBookGenres(Long userId) {
        return userGenrePreferenceRepository
                .findByUserIdAndGenreTypeOrderByRankAsc(userId, GenreType.BOOK)
                .stream()
                .map(UserGenrePreference::getGenreName)
                .collect(Collectors.toList());
    }

    // CD 장르 선호도 계산 및 업데이트
    @Transactional
    public void updateCdGenrePreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MyCd> userCds = myCdRepository.findByUserId(userId);

        if (userCds.isEmpty()) {
            // 선호도가 있으면 삭제
            userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.CD);
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

        // 상위 3개 장르 선별
        List<Map.Entry<String, Integer>> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 기존 선호도 삭제
        userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.CD);

        // 새 선호도 저장
        for (int i = 0; i < topGenres.size(); i++) {
            Map.Entry<String, Integer> entry = topGenres.get(i);
            UserGenrePreference preference = UserGenrePreference.create(
                    user, GenreType.CD, entry.getKey(), entry.getValue(), i + 1
            );
            userGenrePreferenceRepository.save(preference);
        }
    }

    // 책 장르 선호도 계산 및 업데이트
    @Transactional
    public void updateBookGenrePreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MyBook> userBooks = myBookRepository.findAllByUserId(userId);

        if (userBooks.isEmpty()) {
            // 선호도가 있으면 삭제
            userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.BOOK);
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

        // 상위 3개 장르 선별
        List<Map.Entry<String, Integer>> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 기존 선호도 삭제
        userGenrePreferenceRepository.deleteByUserIdAndGenreType(userId, GenreType.BOOK);

        // 새 선호도 저장
        for (int i = 0; i < topGenres.size(); i++) {
            Map.Entry<String, Integer> entry = topGenres.get(i);
            UserGenrePreference preference = UserGenrePreference.create(
                    user, GenreType.BOOK, entry.getKey(), entry.getValue(), i + 1
            );
            userGenrePreferenceRepository.save(preference);
        }
    }
}