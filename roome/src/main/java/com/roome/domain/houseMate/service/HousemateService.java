package com.roome.domain.houseMate.service;

import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.houseMate.dto.HousemateResponseDto;
import com.roome.domain.houseMate.entity.AddedHousemate;
import com.roome.domain.houseMate.notificationEvent.HouseMateCreatedEvent;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousemateService {

  private final HousemateRepository housemateRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher; // 이벤트 발행자
  private final UserActivityService userActivityService;

  // 팔로잉 목록 조회 (내가 추가한 유저 목록)
  public HousemateListResponse getFollowingList(Long userId, Long cursor, int limit,
      String nickname) {
    List<HousemateInfo> housemates = housemateRepository.findByUserId(userId, cursor, limit + 1,
        nickname);
    return createHousemateListResponse(housemates, limit);
  }

  // 팔로워 목록 조회 (나를 추가한 유저 목록)
  public HousemateListResponse getFollowerList(Long userId, Long cursor, int limit,
      String nickname) {
    List<HousemateInfo> housemates = housemateRepository.findByAddedId(userId, cursor, limit + 1,
        nickname);
    return createHousemateListResponse(housemates, limit);
  }

  // 하우스메이트 추가
  @Transactional
  public AddedHousemate addHousemate(Long userId, Long targetId) {
    //userId의 유효성 체크
    userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 대상 유저 존재 체크
    userRepository.findById(targetId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 이미 추가된 하우스메이트 체크
    if (housemateRepository.existsByUserIdAndAddedId(userId, targetId)) {
      throw new BusinessException(ErrorCode.ALREADY_HOUSEMATE);
    }

    AddedHousemate newHousemate = housemateRepository.save(
        AddedHousemate.builder().userId(userId).addedId(targetId).build());

    // 하우스메이트 추가 알림 발행
    log.info("하우스메이트 알림 이벤트 발행: 발신자={}, 수신자={}, 대상 ID={}", userId, targetId, targetId);
    try {
      eventPublisher.publishEvent(new HouseMateCreatedEvent(
          this,
          userId,    // 발신자 (하우스메이트 추가한 사용자)
          targetId,  // 수신자 (하우스메이트로 추가된 사용자)
          targetId   // 대상 ID (하우스메이트로 추가된 사용자의 ID)
      ));
    } catch (Exception e) {
      log.error("하우스메이트 알림 이벤트 발행 중 오류 발생: {}", e.getMessage(), e);
      // 알림 발행 실패가 비즈니스 로직에 영향을 주지 않도록 예외를 잡아서 처리
    }
    return newHousemate; // 저장된 하우스메이트 객체 반환
  }

  // 하우스메이트 삭제
  @Transactional
  public void removeHousemate(Long userId, Long targetId) {
    // 하우스메이트 관계 확인
    if (!housemateRepository.existsByUserIdAndAddedId(userId, targetId)) {
      throw new BusinessException(ErrorCode.NOT_HOUSEMATE);
    }

    housemateRepository.deleteByUserIdAndAddedId(userId, targetId);
  }

  // 하우스메이트 목록 응답 생성
  private HousemateListResponse createHousemateListResponse(List<HousemateInfo> housemates,
      int limit) {
    boolean hasNext = housemates.size() > limit;
    List<HousemateInfo> content = hasNext ? housemates.subList(0, limit) : housemates;
    String nextCursor =
        hasNext ? String.valueOf(content.get(content.size() - 1).getUserId()) : null;

    return HousemateListResponse.builder().housemates(content).nextCursor(nextCursor)
        .hasNext(hasNext).build();
  }

  // 하우스메이트 응답 DTO 생성
  public HousemateResponseDto toResponseDto(AddedHousemate housemate) {
    // 필요한 경우 추가 사용자 정보 조회
    User addedUser = userRepository.findById(housemate.getAddedId())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return HousemateResponseDto.builder()
        .id(housemate.getId())
        .userId(housemate.getUserId())
        .addedId(housemate.getAddedId())
        .createdAt(housemate.getCreatedAt())
        .addedUserName(addedUser.getName())
        .addedUserProfileImageUrl(addedUser.getProfileImage())
        .build();
  }

  // 팔로워 증가 활동 기록 (팔로잉 받은 사람에게 점수 부여)
  public void recordFollowActivity(Long userId, Long targetId) {
    try {
      userActivityService.recordFollowActivity(userId, targetId);
    } catch (Exception e) {
      log.error("팔로워 활동 기록 중 오류 발생: {}", e.getMessage(), e);
    }
  }
}