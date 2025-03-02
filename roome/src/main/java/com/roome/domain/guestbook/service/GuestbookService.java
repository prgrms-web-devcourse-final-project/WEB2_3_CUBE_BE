package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.*;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행자

    public GuestbookListResponseDto getGuestbook(Long roomId, int page, int size) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Page<Guestbook> guestbookPage = guestbookRepository.findByRoom(
                room,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<GuestbookResponseDto> guestbooks = guestbookPage.stream()
                .map(GuestbookResponseDto::from)
                .collect(Collectors.toList());

        return GuestbookListResponseDto.builder()
                .roomId(roomId)
                .guestbook(guestbooks)
                .pagination(PaginationDto.builder()
                        .page(page)
                        .size(size)
                        .totalPages(guestbookPage.getTotalPages())
                        .build())
                .build();
    }

    @Transactional
    public GuestbookResponseDto addGuestbook(Long roomId, Long userId, GuestbookRequestDto requestDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Guestbook guestbook = Guestbook.builder()
                .room(room)
                .user(user)
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .message(requestDto.getMessage())
                .relation(RelationType.지나가던_나그네)
                .createdAt(LocalDateTime.now())
                .build();

        guestbookRepository.save(guestbook);

        // 방명록 보상 포인트 적립
        pointService.addGuestbookReward(userId);

        Long roomOwnerId = room.getUser().getId();
        if (!userId.equals(roomOwnerId)) {
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

        return GuestbookResponseDto.from(guestbook);
    }

    @Transactional
    public GuestbookListResponseDto addGuestbookWithPagination(Long roomId, Long userId, GuestbookRequestDto requestDto, int size) {
        addGuestbook(roomId, userId, requestDto); // 기존 메서드 호출 (방명록 추가)
        return getGuestbook(roomId, 1, size); // 첫 번째 페이지 데이터 반환
    }

    @Transactional
    public void deleteGuestbook(Long guestbookId, Long userId) {
        Guestbook guestbook = guestbookRepository.findById(guestbookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUESTBOOK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!guestbook.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN);
        }

        guestbookRepository.delete(guestbook);
    }
}
