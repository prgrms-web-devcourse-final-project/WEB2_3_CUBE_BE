package com.roome.domain.houseMate.listener;

import com.roome.domain.houseMate.dto.UserStatusDto;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.event.UserStatusChangedEvent;
import com.roome.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusEventListenerTest {

    @Mock
    private HousemateRepository housemateRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private UserStatusEventListener userStatusEventListener;

    private UserStatusChangedEvent event;
    private final Long userId = 1L;
    private final Status newStatus = Status.ONLINE;

    @BeforeEach
    void setUp() {
        event = new UserStatusChangedEvent(this, userId, newStatus);
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 성공 - 팔로워와 팔로잉 모두 존재")
    void handleUserStatusChanged_Success() {
        // given
        List<Long> followerIds = Arrays.asList(2L, 3L);
        List<Long> followingIds = Arrays.asList(4L, 5L);

        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willReturn(followerIds);
        given(housemateRepository.findFollowingIdsByUserId(userId))
                .willReturn(followingIds);

        // when
        userStatusEventListener.handleUserStatusChanged(event);

        // then
        verify(messagingTemplate, times(4)).convertAndSendToUser(
                any(String.class),
                eq("/queue/status-updates"),
                any(UserStatusDto.class)
        );
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 성공 - 팔로워만 존재")
    void handleUserStatusChanged_OnlyFollowers_Success() {
        // given
        List<Long> followerIds = Arrays.asList(2L, 3L);

        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willReturn(followerIds);
        given(housemateRepository.findFollowingIdsByUserId(userId))
                .willReturn(Collections.emptyList());

        // when
        userStatusEventListener.handleUserStatusChanged(event);

        // then
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                any(String.class),
                eq("/queue/status-updates"),
                any(UserStatusDto.class)
        );
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 성공 - 팔로잉만 존재")
    void handleUserStatusChanged_OnlyFollowing_Success() {
        // given
        List<Long> followingIds = Arrays.asList(4L, 5L);

        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willReturn(Collections.emptyList());
        given(housemateRepository.findFollowingIdsByUserId(userId))
                .willReturn(followingIds);

        // when
        userStatusEventListener.handleUserStatusChanged(event);

        // then
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                any(String.class),
                eq("/queue/status-updates"),
                any(UserStatusDto.class)
        );
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 실패 - 메시징 예외 발생")
    void handleUserStatusChanged_MessagingException() {
        // given
        List<Long> followerIds = Collections.singletonList(2L);

        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willReturn(followerIds);

        doThrow(new RuntimeException("Messaging error"))
                .when(messagingTemplate)
                .convertAndSendToUser(
                        any(String.class),
                        any(String.class),
                        any(UserStatusDto.class)
                );

        // when & then
        assertThatThrownBy(() -> userStatusEventListener.handleUserStatusChanged(event))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 실패 - 레포지토리 예외 발생")
    void handleUserStatusChanged_RepositoryException() {
        // given
        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> userStatusEventListener.handleUserStatusChanged(event))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("상태 변경 이벤트 처리 성공 - 팔로워와 팔로잉 모두 없음")
    void handleUserStatusChanged_NoFollowersAndFollowing_Success() {
        // given
        given(housemateRepository.findFollowerIdsByAddedId(userId))
                .willReturn(Collections.emptyList());
        given(housemateRepository.findFollowingIdsByUserId(userId))
                .willReturn(Collections.emptyList());

        // when
        userStatusEventListener.handleUserStatusChanged(event);

        // then
        verify(messagingTemplate, never()).convertAndSendToUser(
                any(String.class),
                any(String.class),
                any(UserStatusDto.class)
        );
    }
}