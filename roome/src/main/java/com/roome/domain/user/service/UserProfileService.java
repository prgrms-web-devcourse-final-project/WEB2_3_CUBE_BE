package com.roome.domain.user.service;

import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.dto.RecommendedUserDto;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final MyCdRepository myCdRepository;
    private final MyBookRepository myBookRepository;
    private final HousemateRepository housemateRepository;

    public UserProfileResponse getUserProfile(Long userId, Long authUserId) {
        // 사용자 정보 한 번만 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. CD 장르 상위 3개 가져오기
        List<String> topCdGenres = getTopCdGenres(userId);
        // 2. 책 장르 상위 3개 가져오기
        List<String> topBookGenres = getTopBookGenres(userId);
        // 3. 유사한 취향을 가진 사용자 추천
        List<User> recommendedUsers = recommendSimilarUsers(userId);
        // 4. DTO로 변환
        List<RecommendedUserDto> recommendedUserDtoList = recommendedUsers.stream()
                .map(user -> RecommendedUserDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());

        boolean isFollowing = false;
        if (!targetUser.getId().equals(authUserId)) { // 자기 자신이 아닌 경우만 확인
            isFollowing = housemateRepository.existsByUserIdAndAddedId(authUserId, userId);
        }


        // 5. 응답 DTO 생성
        return UserProfileResponse.builder()
                .id(userId.toString())
                .nickname(targetUser.getNickname())
                .profileImage(targetUser.getProfileImage())
                .bio(targetUser.getBio())
                .musicGenres(topCdGenres)
                .bookGenres(topBookGenres)
                .isMyProfile(userId.equals(authUserId))
                .isFollowing(isFollowing)
                .recommendedUsers(recommendedUserDtoList)
                .build();
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.getNickname(), request.getBio());
        return getUserProfile(userId, userId);
    }


    private List<String> getTopCdGenres(Long userId) {
        // 1. 사용자의 모든 CD 가져오기
        List<MyCd> userCds = myCdRepository.findByUserId(userId);

        if (userCds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 장르 카운팅
        Map<String, Long> genreCounts = new HashMap<>();
        for (MyCd myCd : userCds) {
            List<String> genres = myCd.getCd().getGenres();
            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0L) + 1);
            }
        }

        // 3. 상위 3개 장르 반환
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 사용자의 책 컬렉션에서 상위 3개 장르 찾기
    private List<String> getTopBookGenres(Long userId) {
        // 1. 사용자의 모든 책 가져오기
        List<MyBook> userBooks = myBookRepository.findAllByUserId(userId);

        if (userBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 장르 카운팅
        Map<String, Long> genreCounts = new HashMap<>();
        for (MyBook myBook : userBooks) {
            // Book 엔티티에서 장르 정보를 가져오는 방식에 따라 코드 조정 필요
            List<String> genres = myBook.getBook().getBookGenres().stream()
                    .map(bg -> bg.getGenre().getName())
                    .collect(Collectors.toList());

            for (String genre : genres) {
                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0L) + 1);
            }
        }

        // 3. 상위 3개 장르 반환
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 유사한 취향을 가진 사용자 추천
    private List<User> recommendSimilarUsers(Long userId) {
        // 1. 현재 사용자의 선호 장르 가져오기
        List<String> topCdGenres = getTopCdGenres(userId);
        List<String> topBookGenres = getTopBookGenres(userId);

        // 선호 장르가 없으면 빈 리스트 반환
        if (topCdGenres.isEmpty() && topBookGenres.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 다른 모든 사용자 가져오기
        List<User> otherUsers = userRepository.findByIdNot(userId);

        // 3. 각 사용자별 유사도 계산
        Map<User, Integer> similarityScores = new HashMap<>();

        for (User otherUser : otherUsers) {
            int score = 0;

            // CD 장르 유사도
            List<String> otherCdGenres = getTopCdGenres(otherUser.getId());
            for (String genre : topCdGenres) {
                if (otherCdGenres.contains(genre)) {
                    score += 1;
                }
            }

            // 책 장르 유사도
            List<String> otherBookGenres = getTopBookGenres(otherUser.getId());
            for (String genre : topBookGenres) {
                if (otherBookGenres.contains(genre)) {
                    score += 1;
                }
            }

            // 유사도 점수 기록
            if (score > 0) {
                similarityScores.put(otherUser, score);
            }
        }

        // 4. 유사도 높은 순으로 정렬하여 상위 5명 반환
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}