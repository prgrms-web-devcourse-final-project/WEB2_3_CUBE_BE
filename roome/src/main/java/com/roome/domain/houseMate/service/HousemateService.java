package com.roome.domain.houseMate.service;

import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.entity.AddedHousemate;
import com.roome.domain.houseMate.repository.AddedHousemateRepository;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousemateService {
    private final AddedHousemateRepository addedHousemateRepository;
    private final UserRepository userRepository;

    //email로 userId 조회
    public Long findUserIdByUserId(String email) {
        return userRepository.findByEmail(email)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                            .getId();
    }

    // 팔로잉 목록 조회 (내가 추가한 유저 목록)
    public HousemateListResponse getFollowingList(Long userId, String cursor, int limit, String nickname) {
        List<HousemateInfo> housemates = addedHousemateRepository.findByUserId(userId, cursor, limit + 1, nickname);
        return createHousemateListResponse(housemates, limit);
    }

    // 팔로워 목록 조회 (나를 추가한 유저 목록)
    public HousemateListResponse getFollowerList(Long userId, String cursor, int limit, String nickname) {
        List<HousemateInfo> housemates = addedHousemateRepository.findByAddedId(userId, cursor, limit + 1, nickname);
        return createHousemateListResponse(housemates, limit);
    }

    // 하우스메이트 추가
    @Transactional
    public void addHousemate(Long userId, Long targetId) {
        //userId의 유효성 체크
        userRepository.findById(userId)
                      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신 추가 체크
        if (userId.equals(targetId)) {
            throw new BusinessException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        // 대상 유저 존재 체크
        userRepository.findById(targetId)
                      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 추가된 하우스메이트 체크
        if (addedHousemateRepository.existsByUserIdAndAddedId(userId, targetId)) {
            throw new BusinessException(ErrorCode.ALREADY_HOUSEMATE);
        }

        addedHousemateRepository.save(AddedHousemate
                                              .builder()
                                              .userId(userId)
                                              .addedId(targetId)
                                              .build());
    }

    // 하우스메이트 삭제
    @Transactional
    public void removeHousemate(Long userId, Long targetId) {
        // 하우스메이트 관계 확인
        if (!addedHousemateRepository.existsByUserIdAndAddedId(userId, targetId)) {
            throw new BusinessException(ErrorCode.NOT_HOUSEMATE);
        }

        addedHousemateRepository.deleteByUserIdAndAddedId(userId, targetId);
    }

    // 하우스메이트 목록 응답 생성
    private HousemateListResponse createHousemateListResponse(List<HousemateInfo> housemates, int limit) {
        boolean hasNext = housemates.size() > limit;
        List<HousemateInfo> content = hasNext ? housemates.subList(0, limit) : housemates;
        String nextCursor = hasNext ? String.valueOf(content.get(content.size() - 1).getUserId()) : null;

        return HousemateListResponse.builder()
                                    .housemates(content)
                                    .nextCursor(nextCursor)
                                    .hasNext(hasNext)
                                    .build();
    }
}