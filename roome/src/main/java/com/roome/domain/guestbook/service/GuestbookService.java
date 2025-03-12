package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.dto.GuestbookResponseDto;
import com.roome.domain.guestbook.dto.PaginationDto;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestbookService {

  private final GuestbookRepository guestbookRepository;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final HousemateRepository housemateRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final PointService pointService;
  private final ApplicationEventPublisher eventPublisher; // 이벤트 발행자
  private final UserActivityService userActivityService;

  public GuestbookListResponseDto getGuestbook(Long roomId, int page, int size) {
    Room room = roomRepository.findById(roomId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

    Page<Guestbook> guestbookPage = guestbookRepository.findByRoom(
        room,
        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

    Long roomOwnerId = room.getUser().getId();

    List<Guestbook> guestbooks = guestbookPage.getContent();

    List<Long> userIds = guestbooks.stream()
            .map(guestbook -> guestbook.getUser().getId())
            .distinct()
            .collect(Collectors.toList());

    Map<Long, Boolean> housemateStatusMap = userIds.stream()
            .collect(Collectors.toMap(
                    userId -> userId,
                    userId -> housemateRepository.existsByUserIdAndAddedId(roomOwnerId, userId)
            ));

    List<GuestbookResponseDto> guestbookResponses = guestbooks.stream()
            .map(guestbook -> {
              boolean isHousemate = housemateStatusMap.getOrDefault(guestbook.getUser().getId(), false);
              return GuestbookResponseDto.from(guestbook, isHousemate);
            })
            .collect(Collectors.toList());

    return GuestbookListResponseDto.builder()
            .roomId(roomId)
            .guestbook(guestbookResponses)
            .pagination(PaginationDto.builder()
                    .page(page)
                    .size(size)
                    .totalPages(guestbookPage.getTotalPages())
                    .build())
            .build();
  }

  @Transactional
  public void addGuestbook(Long roomId, Long userId, GuestbookRequestDto requestDto) {
    Room room = roomRepository.findById(roomId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Long roomOwnerId = room.getUser().getId();
    boolean isSelfRoom = userId.equals(roomOwnerId); // 본인 방인지 확인

    boolean isHousemate = housemateRepository.existsByUserIdAndAddedId(roomOwnerId, userId);

    Guestbook guestbook = Guestbook.builder()
        .room(room)
        .user(user)
        .nickname(user.getNickname())
        .profileImage(user.getProfileImage())
        .message(requestDto.getMessage())
        .relation(isHousemate ? RelationType.하우스메이트 : RelationType.지나가던_나그네) // 하우스메이트 여부 반영
        .createdAt(LocalDateTime.now())
        .build();

    guestbookRepository.save(guestbook);

    // 방명록 보상 포인트 적립 (본인 방명록 제외)
    if (!isSelfRoom) {

      // 방명록 작성 활동 기록 - 길이 체크
      userActivityService.recordUserActivity(userId, ActivityType.GUESTBOOK, roomId,
          requestDto.getMessage().length());

      boolean hasEarnedToday = pointHistoryRepository.existsRecentEarned(user.getId(), PointReason.GUESTBOOK_REWARD);
      if (!hasEarnedToday) {
        pointService.earnPoints(user, PointReason.GUESTBOOK_REWARD);
        log.info("방명록 포인트 적립 완료 (중복 체크 없이) - User={}, Points=10", userId);
      }

    }

    if (!isSelfRoom) {
      log.info("방명록 알림 이벤트 발행: 발신자={}, 수신자={}, 방명록={}",
          userId, roomOwnerId, guestbook.getGuestbookId());

      try {
        eventPublisher.publishEvent(new GuestBookCreatedEvent(
            this,
            userId,          // 발신자 (방명록 작성자)
            roomOwnerId,     // 수신자 (방 소유자)
            guestbook.getGuestbookId() // 방명록 ID
        ));
      } catch (Exception e) {
        log.error("방명록 알림 이벤트 발행 중 오류 발생: {}", e.getMessage(), e);
        // 알림 발행 실패가 비즈니스 로직에 영향을 주지 않도록 예외를 잡아서 처리
      }
    }

    GuestbookResponseDto.from(guestbook);
  }

  @Transactional
  public GuestbookListResponseDto addGuestbookWithPagination(Long roomId, Long userId,
      GuestbookRequestDto requestDto, int size) {
    addGuestbook(roomId, userId, requestDto); // 기존 메서드 호출 (방명록 추가)
    return getGuestbook(roomId, 1, size); // 첫 번째 페이지 데이터 반환
  }

  @Transactional
  public void deleteGuestbook(Long guestbookId, Long userId) {
    Guestbook guestbook = guestbookRepository.findById(guestbookId)
        .orElseThrow(() -> new BusinessException(ErrorCode.GUESTBOOK_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Long roomOwnerId = guestbook.getRoom().getUser().getId();
    boolean isOwner = roomOwnerId.equals(userId);
    boolean isWriter = guestbook.getUser().equals(user);

    if (!isOwner && !isWriter) { // 둘 다 아니면 예외 발생
      throw new BusinessException(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN);
    }

    guestbookRepository.delete(guestbook);
  }
}
