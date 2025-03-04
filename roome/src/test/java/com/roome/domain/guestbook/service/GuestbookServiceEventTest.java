//package com.roome.domain.guestbook.service;
//
//import com.roome.domain.guestbook.dto.GuestbookRequestDto;
//import com.roome.domain.guestbook.entity.Guestbook;
//import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
//import com.roome.domain.guestbook.repository.GuestbookRepository;
//import com.roome.domain.point.service.PointService;
//import com.roome.domain.room.entity.Room;
//import com.roome.domain.room.repository.RoomRepository;
//import com.roome.domain.user.entity.Provider;
//import com.roome.domain.user.entity.Status;
//import com.roome.domain.user.entity.User;
//import com.roome.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class GuestbookServiceEventTest {
//
//    @Mock
//    private GuestbookRepository guestbookRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PointService pointService;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @InjectMocks
//    private GuestbookService guestbookService;
//
//    @Test
//    @DisplayName("다른 사용자의 방명록 작성 시 이벤트가 발행되어야 함")
//    void addGuestbook_ShouldPublishEvent_WhenUserIsNotRoomOwner() {
//        // Given
//        Long userId = 1L;
//        Long roomOwnerId = 2L; // 다른 사용자
//        Long roomId = 3L;
//
//        // User 객체를 Builder 패턴으로 생성
//        User user = User.builder()
//                .id(userId)
//                .nickname("User1")
//                .profileImage("profile.jpg")
//                .email("user1@example.com")
//                .name("User One")
//                .provider(Provider.GOOGLE)
//                .providerId("google-id-1")
//                .status(Status.ONLINE)
//                .build();
//
//        User roomOwner = User.builder()
//                .id(roomOwnerId)
//                .email("owner@example.com")
//                .name("Room Owner")
//                .provider(Provider.GOOGLE)
//                .providerId("google-id-2")
//                .status(Status.ONLINE)
//                .build();
//
//        Room room = mock(Room.class);
//        when(room.getUser()).thenReturn(roomOwner);
//
//        // 불필요한 stubbing 제거: savedGuestbook.getGuestbookId()
//        Guestbook savedGuestbook = mock(Guestbook.class);
//
//        // GuestbookRequestDto 생성
//        GuestbookRequestDto requestDto = GuestbookRequestDto.builder().message("Hello, nice room!").build();
//
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(savedGuestbook);
//        doNothing().when(pointService).addGuestbookReward(userId);
//
//        // When
//        guestbookService.addGuestbook(roomId, userId, requestDto);
//
//        // Then
//        verify(eventPublisher, times(1)).publishEvent(any(GuestBookCreatedEvent.class));
//    }
//
//    @Test
//    @DisplayName("자신의 방명록 작성 시 이벤트가 발행되지 않아야 함")
//    void addGuestbook_ShouldNotPublishEvent_WhenUserIsRoomOwner() {
//        // Given
//        Long userId = 1L; // 같은 사용자 (방의 소유자)
//        Long roomId = 3L;
//
//        // User 객체를 Builder 패턴으로 생성
//        User user = User.builder()
//                .id(userId)
//                .nickname("User1")
//                .profileImage("profile.jpg")
//                .email("user1@example.com")
//                .name("User One")
//                .provider(Provider.GOOGLE)
//                .providerId("google-id-1")
//                .status(Status.OFFLINE)
//                .build();
//
//        Room room = mock(Room.class);
//        when(room.getUser()).thenReturn(user);
//
//        Guestbook savedGuestbook = mock(Guestbook.class);
//
//        // GuestbookRequestDto 생성
//        GuestbookRequestDto requestDto = GuestbookRequestDto.builder().message("Hello, nice room!").build();
//
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(savedGuestbook);
//        doNothing().when(pointService).addGuestbookReward(userId);
//
//        // When
//        guestbookService.addGuestbook(roomId, userId, requestDto);
//
//        // Then
//        verify(eventPublisher, never()).publishEvent(any(GuestBookCreatedEvent.class));
//    }
//
//    @Test
//    @DisplayName("이벤트 발행 중 예외가 발생해도 정상적으로 처리되어야 함")
//    void addGuestbook_ShouldHandleEventPublishingException() {
//        // Given
//        Long userId = 1L;
//        Long roomOwnerId = 2L; // 다른 사용자
//        Long roomId = 3L;
//
//        // User 객체를 Builder 패턴으로 생성
//        User user = User.builder()
//                .id(userId)
//                .nickname("User1")
//                .profileImage("profile.jpg")
//                .email("user1@example.com")
//                .name("User One")
//                .provider(Provider.GOOGLE)
//                .providerId("google-id-1")
//                .status(Status.OFFLINE)
//                .build();
//
//        User roomOwner = User.builder()
//                .id(roomOwnerId)
//                .email("owner@example.com")
//                .name("Room Owner")
//                .provider(Provider.GOOGLE)
//                .providerId("google-id-2")
//                .status(Status.ONLINE)
//                .build();
//
//        Room room = mock(Room.class);
//        when(room.getUser()).thenReturn(roomOwner);
//
//        // 불필요한 stubbing 제거: savedGuestbook.getGuestbookId()
//        Guestbook savedGuestbook = mock(Guestbook.class);
//
//        // GuestbookRequestDto 생성
//        GuestbookRequestDto requestDto = GuestbookRequestDto.builder().message("Hello, nice room!").build();
//
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(guestbookRepository.save(any(Guestbook.class))).thenReturn(savedGuestbook);
//        doNothing().when(pointService).addGuestbookReward(userId);
//
//        doThrow(new RuntimeException("Event publishing error")).when(eventPublisher).publishEvent(any(GuestBookCreatedEvent.class));
//
//        // When & Then - 예외를 잡아서 처리하므로 테스트가 통과해야 함
//        guestbookService.addGuestbook(roomId, userId, requestDto);
//
//        // 이벤트 발행 시도가 있었는지 확인
//        verify(eventPublisher, times(1)).publishEvent(any(GuestBookCreatedEvent.class));
//    }
//}