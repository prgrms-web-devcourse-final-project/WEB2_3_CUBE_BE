package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GuestbookServiceEventTest {

    @InjectMocks
    private GuestbookService guestbookService;

    @Mock
    private GuestbookRepository guestbookRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointService pointService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HousemateRepository housemateRepository;
    @Mock
    private UserActivityService userActivityService;
    @Mock
    private PointHistoryRepository pointHistoryRepository;


    private User user;
    private User roomOwner;
    private Room room;
    private GuestbookRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .nickname("User1")
                .profileImage("profile.jpg")
                .email("user1@example.com")
                .name("User One")
                .provider(Provider.GOOGLE)
                .providerId("google-id-1")
                .status(Status.ONLINE)
                .build();

        roomOwner = User.builder()
                .id(2L)
                .email("owner@example.com")
                .name("Room Owner")
                .provider(Provider.GOOGLE)
                .providerId("google-id-2")
                .status(Status.ONLINE)
                .build();

        room = mock(Room.class);
        when(room.getUser()).thenReturn(roomOwner);

        requestDto = GuestbookRequestDto.builder()
                .message("Hello, nice room!")
                .build();
    }

    @Test
    @DisplayName("다른 사용자의 방명록 작성 시 이벤트가 발행되어야 함")
    void testAddGuestbook_PublishEvent_WhenUserIsNotRoomOwner() {
        // Given
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(mock(Guestbook.class));

        // ✅ housemateRepository Stubbing 추가
        when(housemateRepository.existsByUserIdAndAddedId(1L, 2L)).thenReturn(false);

        // When
        guestbookService.addGuestbook(3L, 1L, requestDto);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(GuestBookCreatedEvent.class));
    }


    @Test
    @DisplayName("자신의 방명록 작성 시 이벤트가 발행되지 않아야 함")
    void testAddGuestbook_DoNotPublishEvent_WhenUserIsRoomOwner() {
        // Given
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(roomOwner));
        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(mock(Guestbook.class));

        // When
        guestbookService.addGuestbook(3L, 2L, requestDto);

        // Then
        verify(eventPublisher, never()).publishEvent(any(GuestBookCreatedEvent.class));
    }

    @Test
    @DisplayName("이벤트 발행 중 예외가 발생해도 정상적으로 처리되어야 함")
    void testAddGuestbook_HandleEventPublishingException() {
        // Given
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(mock(Guestbook.class));

        doThrow(new RuntimeException("Event publishing error"))
                .when(eventPublisher).publishEvent(any(GuestBookCreatedEvent.class));

        // When & Then (예외가 발생해도 정상 처리되어야 함)
        guestbookService.addGuestbook(3L, 1L, requestDto);

        // 이벤트 발행 시도는 있었지만 예외가 발생한 것 확인
        verify(eventPublisher, times(1)).publishEvent(any(GuestBookCreatedEvent.class));
    }

    @Test
    @DisplayName("방명록 추가 실패 - 존재하지 않는 방")
    void testAddGuestbook_Fail_RoomNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                guestbookService.addGuestbook(999L, 1L, requestDto));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방명록 추가 실패 - 존재하지 않는 사용자")
    void testAddGuestbook_Fail_UserNotFound() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                guestbookService.addGuestbook(3L, 999L, requestDto));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방명록을 추가하면 첫 번째 페이지 데이터가 반환되어야 함")
    void testAddGuestbookWithPagination_Success() {
        // Given
        Long roomId = 3L;
        Long userId = 1L;
        int size = 10;

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(mock(Guestbook.class));
        when(housemateRepository.existsByUserIdAndAddedId(userId, room.getUser().getId())).thenReturn(false);
        when(userActivityService.recordUserActivity(anyLong(), any(), anyLong(), anyInt())).thenReturn(true);
        when(pointHistoryRepository.existsRecentEarned(userId, PointReason.GUESTBOOK_REWARD)).thenReturn(false);
        doNothing().when(pointService).earnPoints(any(User.class), any(PointReason.class));
        when(guestbookRepository.findByRoom(any(Room.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        // When
        GuestbookListResponseDto response = guestbookService.addGuestbookWithPagination(roomId, userId, requestDto, size);

        // Then
        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertNotNull(response.getPagination());

        verify(guestbookRepository, times(1)).save(any(Guestbook.class));
        verify(guestbookRepository, times(1)).findByRoom(any(Room.class), any(PageRequest.class));
    }


}
