package com.roome.domain.user.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserGenreService userGenreService;

    public UserProfileResponse getUserProfile(Long targetUserId, Long currentUserId) {
        User user = userRepository.getById(targetUserId);

        return UserProfileResponse.of(
                user,
                userGenreService.getUserMusicGenres(targetUserId),
                userGenreService.getUserBookGenres(targetUserId),
                targetUserId.equals(currentUserId)
                                     );
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.getById(userId);

        validateNickname(request.getNickname());
        validateBio(request.getBio());

        user.updateProfile(
                request.getNickname(),
                user.getProfileImage(), // 프로필 이미지는 별도 API로 처리
                request.getBio()
                          );

        return getUserProfile(userId, userId);
    }

    private void validateNickname(String nickname) {
        if (!nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$")) {
            throw new BusinessException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }
    }

    private void validateBio(String bio) {
        if (bio != null && bio.length() > 30) {
            throw new BusinessException(ErrorCode.INVALID_BIO_LENGTH);
        }
    }
}
