package com.roome.domain.cdcomment.service;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.exception.CdCommentListEmptyException;
import com.roome.domain.cdcomment.exception.CdCommentNotFoundException;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CdCommentServiceTest {

  @Mock private CdCommentRepository cdCommentRepository;
  @Mock private MyCdRepository myCdRepository;
  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private UserActivityService userActivityService;

  @InjectMocks private CdCommentService cdCommentService;

  private User user;
  private User roomOwner;
  private MyCd myCd;
  private CdComment cdComment;

  @BeforeEach
  void setUp() {
    user = User.builder().id(1L).nickname("현구").build();
    roomOwner = User.builder().id(2L).nickname("방 주인").build();
    myCd = MyCd.builder().id(1L).user(roomOwner).build();
    cdComment = CdComment.builder().id(1L).user(user).myCd(myCd).timestamp(220).content("이 곡 최고네요!").build();
  }

  @Test
  @DisplayName("댓글 추가 성공")
  void addComment_Success() {
    CdCommentCreateRequest request = new CdCommentCreateRequest(220, "이 곡 최고네요!");

    when(myCdRepository.findById(1L)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(cdComment);

    // 이벤트 발행을 Mock 처리
    doNothing().when(eventPublisher).publishEvent(any());

    // 활동 기록을 Mock 처리 (doAnswer 사용)
    doAnswer(invocation -> null).when(userActivityService).recordUserActivity(anyLong(), any(), anyLong());

    CdCommentResponse response = cdCommentService.addComment(1L, 1L, request);

    assertThat(response.getContent()).isEqualTo(request.getContent());
    assertThat(response.getTimestamp()).isEqualTo(request.getTimestamp());
  }

  @Test
  @DisplayName("CD 전체 댓글 목록 조회 성공")
  void getAllComments_Success() {
    List<CdComment> comments = List.of(cdComment);

    when(cdCommentRepository.findByMyCdId(1L)).thenReturn(comments);

    List<CdCommentResponse> response = cdCommentService.getAllComments(1L);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("CD 전체 댓글 목록이 비어있을 때 예외 발생")
  void getAllComments_EmptyList() {
    when(cdCommentRepository.findByMyCdId(1L)).thenReturn(List.of());

    assertThatThrownBy(() -> cdCommentService.getAllComments(1L))
        .isInstanceOf(CdCommentListEmptyException.class);
  }

  @Test
  @DisplayName("댓글 목록 조회 성공 - 키워드 없음")
  void getComments_Success_NoKeyword() {
    PageRequest pageRequest = PageRequest.of(0, 5);
    Page<CdComment> commentPage = new PageImpl<>(List.of(cdComment), pageRequest, 1);

    when(cdCommentRepository.findByMyCdId(1L, pageRequest)).thenReturn(commentPage);

    CdCommentListResponse response = cdCommentService.getComments(1L, null, 0, 5);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("댓글 목록 조회 성공 - 키워드 있음")
  void getComments_Success_WithKeyword() {
    PageRequest pageRequest = PageRequest.of(0, 5);
    Page<CdComment> commentPage = new PageImpl<>(List.of(cdComment), pageRequest, 1);

    when(cdCommentRepository.findByMyCdIdAndKeyword(1L, "테스트", pageRequest)).thenReturn(commentPage);

    CdCommentListResponse response = cdCommentService.getComments(1L, "테스트", 0, 5);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 (댓글 없음)")
  void getComments_EmptyList() {
    PageRequest pageRequest = PageRequest.of(0, 5);
    Page<CdComment> emptyPage = Page.empty(pageRequest);

    when(cdCommentRepository.findByMyCdId(1L, pageRequest)).thenReturn(emptyPage);

    assertThatThrownBy(() -> cdCommentService.getComments(1L, null, 0, 5))
        .isInstanceOf(CdCommentListEmptyException.class);
  }

  @Test
  @DisplayName("댓글 삭제 성공 (댓글 작성자)")
  void deleteComment_Success_ByAuthor() {
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(cdComment));

    cdCommentService.deleteComment(1L, 1L);

    verify(cdCommentRepository, times(1)).delete(cdComment);
  }

  @Test
  @DisplayName("댓글 삭제 성공 (방 주인)")
  void deleteComment_Success_ByRoomOwner() {
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(cdComment));

    cdCommentService.deleteComment(2L, 1L);

    verify(cdCommentRepository, times(1)).delete(cdComment);
  }

  @Test
  @DisplayName("댓글 삭제 실패 (권한 없음)")
  void deleteComment_Unauthorized() {
    User anotherUser = User.builder().id(3L).nickname("다른 사용자").build();
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(cdComment));

    assertThatThrownBy(() -> cdCommentService.deleteComment(3L, 1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
  void deleteComment_NotFound() {
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cdCommentService.deleteComment(1L, 1L))
        .isInstanceOf(CdCommentNotFoundException.class);
  }
}
