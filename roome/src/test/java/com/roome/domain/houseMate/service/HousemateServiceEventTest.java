package com.roome.domain.houseMate.service;

import com.roome.domain.houseMate.entity.AddedHousemate;
import com.roome.domain.houseMate.notificationEvent.HouseMateCreatedEvent;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HousemateServiceEventTest {

    @Mock
    private HousemateRepository housemateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private HousemateService housemateService;

    @Test
    @DisplayName("하우스메이트 추가 시 알림 이벤트가 발행되어야 한다")
    void addHousemate_ShouldPublishEvent() {
        // Given
        Long userId = 1L;
        Long targetId = 2L;

        // User 객체를 Builder 패턴으로 생성
        User user = User.builder()
                .id(userId)
                .nickname("User1")
                .name("User One")
                .email("user1@example.com")
                .provider(Provider.GOOGLE)
                .providerId("google-id-1")
                .status(Status.ONLINE)
                .build();

        User targetUser = User.builder()
                .id(targetId)
                .nickname("User2")
                .name("User Two")
                .email("user2@example.com")
                .provider(Provider.GOOGLE)
                .providerId("google-id-2")
                .status(Status.ONLINE)
                .build();

        AddedHousemate newHousemate = AddedHousemate.builder().userId(userId).addedId(targetId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(housemateRepository.existsByUserIdAndAddedId(userId, targetId)).thenReturn(false);
        when(housemateRepository.save(any(AddedHousemate.class))).thenReturn(newHousemate);

        // When
        housemateService.addHousemate(userId, targetId);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(HouseMateCreatedEvent.class));
    }

    @Test
    @DisplayName("하우스메이트 추가 시 이벤트 발행 중 예외가 발생해도 정상적으로 처리되어야 한다")
    void addHousemate_ShouldHandleEventPublishingException() {
        // Given
        Long userId = 1L;
        Long targetId = 2L;

        // User 객체를 Builder 패턴으로 생성
        User user = User.builder()
                .id(userId)
                .nickname("User1")
                .name("User One")
                .email("user1@example.com")
                .provider(Provider.GOOGLE)
                .providerId("google-id-1")
                .status(Status.ONLINE)
                .build();

        User targetUser = User.builder()
                .id(targetId)
                .nickname("User2")
                .name("User Two")
                .email("user2@example.com")
                .provider(Provider.GOOGLE)
                .providerId("google-id-2")
                .status(Status.ONLINE)
                .build();

        AddedHousemate newHousemate = AddedHousemate.builder().userId(userId).addedId(targetId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(housemateRepository.existsByUserIdAndAddedId(userId, targetId)).thenReturn(false);
        when(housemateRepository.save(any(AddedHousemate.class))).thenReturn(newHousemate);

        doThrow(new RuntimeException("Event publishing error")).when(eventPublisher).publishEvent(any(HouseMateCreatedEvent.class));

        // When & Then - 예외를 잡아서 처리하므로 테스트가 통과해야 함
        housemateService.addHousemate(userId, targetId);

        // 이벤트 발행 시도가 있었는지 확인
        verify(eventPublisher, times(1)).publishEvent(any(HouseMateCreatedEvent.class));
    }
}