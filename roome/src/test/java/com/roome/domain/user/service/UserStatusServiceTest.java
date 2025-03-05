package com.roome.domain.user.service;

import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.event.UserStatusChangedEvent;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusServiceTest {

    @InjectMocks
    private UserStatusService userStatusService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User user;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = User.create(
                "Test User",
                "testuser",
                "test@example.com",
                "profile.jpg",
                Provider.GOOGLE,
                "google123",
                LocalDateTime.now()
        );
        // Set ID for the user
        user.setId(userId);
    }

    @Test
    @DisplayName("상태가 변경된 경우 이벤트가 발행되어야 한다")
    void updateUserStatus_StatusChanged_ShouldUpdateAndPublishEvent() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ArgumentCaptor<UserStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserStatusChangedEvent.class);

        // 현재 상태는 ONLINE (User.create 메서드에서 설정됨)
        assertEquals(Status.ONLINE, user.getStatus());

        // When
        userStatusService.updateUserStatus(userId, Status.OFFLINE);

        // Then
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserStatusChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(Status.OFFLINE, capturedEvent.getStatus());
        assertEquals(Status.OFFLINE, user.getStatus());
    }

    @Test
    @DisplayName("상태가 동일한 경우 업데이트 및 이벤트 발행이 일어나지 않아야 한다")
    void updateUserStatus_SameStatus_ShouldNotUpdateOrPublishEvent() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 현재 상태는 ONLINE
        assertEquals(Status.ONLINE, user.getStatus());

        // When
        userStatusService.updateUserStatus(userId, Status.ONLINE);

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserStatusChangedEvent.class));
        assertEquals(Status.ONLINE, user.getStatus()); // 상태 변경 없음
    }

    @Test
    @DisplayName("사용자가 존재하지 않는 경우 아무 작업도 수행하지 않아야 한다")
    void updateUserStatus_UserNotFound_ShouldDoNothing() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        userStatusService.updateUserStatus(999L, Status.OFFLINE);

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserStatusChangedEvent.class));
    }
}