package com.roome.domain.user.service;

import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.recommendedUser.service.RecommendedUserService;
import com.roome.domain.user.dto.RecommendedUserDto;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.userGenrePreference.service.GenrePreferenceService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final HousemateRepository housemateRepository;
    private final GenrePreferenceService genrePreferenceService;
    private final RecommendedUserService recommendedUserService;

    public UserProfileResponse getUserProfile(Long userId, Long authUserId) {
        // 사용자 정보 한 번만 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. CD 장르 상위 3개 가져오기 - 저장된 선호도 사용
        List<String> topCdGenres = genrePreferenceService.getTopCdGenres(userId);

        // 2. 책 장르 상위 3개 가져오기 - 저장된 선호도 사용
        List<String> topBookGenres = genrePreferenceService.getTopBookGenres(userId);

        // 3. 유사한 취향을 가진 사용자 추천 - 배치로 저장된 데이터 사용
        List<RecommendedUserDto> recommendedUserDtoList = recommendedUserService.getRecommendedUsers(userId);

        boolean isFollowing = false;
        if (!targetUser.getId().equals(authUserId)) { // 자기 자신이 아닌 경우만 확인
            isFollowing = housemateRepository.existsByUserIdAndAddedId(authUserId, userId);
        }

        // 응답 DTO 생성
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
}