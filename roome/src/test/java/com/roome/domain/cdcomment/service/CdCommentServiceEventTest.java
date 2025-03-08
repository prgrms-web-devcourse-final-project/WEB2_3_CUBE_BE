package com.roome.domain.cdcomment.service;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.notificationEvent.CdCommentCreatedEvent;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CdCommentServiceEventTest {

  @Mock
  private CdCommentRepository cdCommentRepository;

  @Mock
  private MyCdRepository myCdRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private CdCommentService cdCommentService;

  @Mock
  private UserActivityService userActivityService;

  @Test
  void addComment_ShouldPublishEvent_WhenUserIsNotOwner() {
    // Given
    Long userId = 1L;
    Long cdOwnerId = 2L; // 다른 사용자
    Long cdId = 3L;
    Long commentId = 4L;

    User commentUser = User.builder()
        .id(userId)
        .nickname("User1")
        .email("user1@example.com")
        .name("User One")
        .provider(Provider.GOOGLE)
        .providerId("google-id-1")
        .status(Status.ONLINE)
        .build();

    User ownerUser = User.builder()
        .id(cdOwnerId)
        .nickname("User2")
        .email("user2@example.com")
        .name("User Two")
        .provider(Provider.GOOGLE)
        .providerId("google-id-2")
        .status(Status.ONLINE)
        .build();

    MyCd myCd = mock(MyCd.class);
    when(myCd.getUser()).thenReturn(ownerUser);
    when(myCd.getId()).thenReturn(cdId);

    CdComment savedComment = mock(CdComment.class);
    when(savedComment.getId()).thenReturn(commentId);
    when(savedComment.getMyCd()).thenReturn(myCd);
    when(savedComment.getUser()).thenReturn(commentUser);

    // DTO 객체 (timestamp를 정수형으로 변경)
    CdCommentCreateRequest request = new CdCommentCreateRequest(30, "Test comment");

    when(myCdRepository.findById(cdId)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(userId)).thenReturn(Optional.of(commentUser));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(savedComment);

    // When
    cdCommentService.addComment(userId, cdId, request);

    // Then
    ArgumentCaptor<CdCommentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CdCommentCreatedEvent.class);
    verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

    CdCommentCreatedEvent capturedEvent = eventCaptor.getValue();
    assertEquals(userId, capturedEvent.getSenderId());
    assertEquals(cdOwnerId, capturedEvent.getReceiverId());
    assertEquals(cdId, capturedEvent.getCdId());
    assertEquals(commentId, capturedEvent.getCommentId());
  }

  @Test
  void addComment_ShouldNotPublishEvent_WhenUserIsOwner() {
    // Given
    Long userId = 1L; // 같은 사용자 (CD의 소유자)
    Long cdId = 3L;

    User user = User.builder()
        .id(userId)
        .nickname("User1")
        .email("user1@example.com")
        .name("User One")
        .provider(Provider.GOOGLE)
        .providerId("google-id-1")
        .status(Status.ONLINE)
        .build();

    MyCd myCd = mock(MyCd.class);
    when(myCd.getUser()).thenReturn(user);
    when(myCd.getId()).thenReturn(cdId);

    CdComment savedComment = mock(CdComment.class);
    when(savedComment.getMyCd()).thenReturn(myCd);
    when(savedComment.getUser()).thenReturn(user);

    // DTO 객체 (timestamp를 정수형으로 변경)
    CdCommentCreateRequest request = new CdCommentCreateRequest(30, "Test comment");

    when(myCdRepository.findById(cdId)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(savedComment);

    // When
    cdCommentService.addComment(userId, cdId, request);

    // Then
    verify(eventPublisher, never()).publishEvent(any(CdCommentCreatedEvent.class));
  }

  @Test
  void addComment_ShouldHandleEventPublishingException() {
    // Given
    Long userId = 1L;
    Long cdOwnerId = 2L; // 다른 사용자
    Long cdId = 3L;
    Long commentId = 4L;

    User commentUser = User.builder()
        .id(userId)
        .nickname("User1")
        .email("user1@example.com")
        .name("User One")
        .provider(Provider.GOOGLE)
        .providerId("google-id-1")
        .status(Status.ONLINE)
        .build();

    User ownerUser = User.builder()
        .id(cdOwnerId)
        .nickname("User2")
        .email("user2@example.com")
        .name("User Two")
        .provider(Provider.GOOGLE)
        .providerId("google-id-2")
        .status(Status.ONLINE)
        .build();

    MyCd myCd = mock(MyCd.class);
    when(myCd.getUser()).thenReturn(ownerUser);
    when(myCd.getId()).thenReturn(cdId);

    CdComment savedComment = mock(CdComment.class);
    when(savedComment.getId()).thenReturn(commentId);
    when(savedComment.getMyCd()).thenReturn(myCd);
    when(savedComment.getUser()).thenReturn(commentUser);

    // DTO 객체 (timestamp를 정수형으로 변경)
    CdCommentCreateRequest request = new CdCommentCreateRequest(30, "Test comment");

    when(myCdRepository.findById(cdId)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(userId)).thenReturn(Optional.of(commentUser));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(savedComment);

    doThrow(new RuntimeException("Event publishing error")).when(eventPublisher).publishEvent(any(CdCommentCreatedEvent.class));

    // When & Then - 예외를 잡아서 처리하므로 테스트가 통과해야 함
    cdCommentService.addComment(userId, cdId, request);

    // 이벤트 발행 시도가 있었는지 확인
    verify(eventPublisher, times(1)).publishEvent(any(CdCommentCreatedEvent.class));
  }
}