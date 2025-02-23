package com.roome.domain.cdcomment.service;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.exception.CdCommentListEmptyException;
import com.roome.domain.cdcomment.exception.CdCommentNotFoundException;
import com.roome.domain.cdcomment.exception.CdCommentSearchEmptyException;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 초기화 자동 처리
class CdCommentServiceTest {

  @Mock
  private CdCommentRepository cdCommentRepository;

  @Mock
  private MyCdRepository myCdRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CdCommentService cdCommentService;

  private User user;
  private MyCd myCd;
  private CdComment cdComment;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .nickname("현구")
        .build();

    myCd = MyCd.builder()
        .id(1L)
        .build();

    cdComment = CdComment.builder()
        .id(1L)
        .user(user)
        .myCd(myCd)
        .timestamp("03:40")
        .content("이 곡 최고네요!")
        .build();

  }

  @Test
  @DisplayName("댓글 추가 성공")
  void addComment_Success() {
    // given
    CdCommentCreateRequest request = new CdCommentCreateRequest("03:40", "이 곡 최고네요!");

    when(myCdRepository.findById(1L)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(cdComment);

    // when
    CdCommentResponse response = cdCommentService.addComment(1L, 1L, request);

    // then
    assertThat(response.getContent()).isEqualTo(request.getContent());
    assertThat(response.getTimestamp()).isEqualTo(request.getTimestamp());
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void getComments_Success() {
    // given
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> page = new PageImpl<>(List.of(cdComment), pageable, 1);

    when(cdCommentRepository.findByMyCdId(1L, pageable)).thenReturn(page);

    // when
    CdCommentListResponse response = cdCommentService.getComments(1L, 0, 5);

    // then
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("댓글 목록이 비어있을 때 예외 발생")
  void getComments_EmptyList() {
    // given
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> emptyPage = Page.empty(pageable);

    when(cdCommentRepository.findByMyCdId(1L, pageable)).thenReturn(emptyPage);

    // when & then
    assertThatThrownBy(() -> cdCommentService.getComments(1L, 0, 5))
        .isInstanceOf(CdCommentListEmptyException.class);
  }

  @Test
  @DisplayName("댓글 검색 성공")
  void searchComments_Success() {
    // given
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> page = new PageImpl<>(List.of(cdComment), pageable, 1);

    when(cdCommentRepository.findByMyCdIdAndKeyword(1L, "최고", pageable))
        .thenReturn(page);

    // when
    CdCommentListResponse response = cdCommentService.searchComments(1L, "최고", 0, 5);

    // then
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("댓글 검색 결과가 없을 때 예외 발생")
  void searchComments_EmptyResult() {
    // given
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> emptyPage = Page.empty(pageable);

    when(cdCommentRepository.findByMyCdIdAndKeyword(1L, "없는내용", pageable))
        .thenReturn(emptyPage);

    // when & then
    assertThatThrownBy(() -> cdCommentService.searchComments(1L, "없는내용", 0, 5))
        .isInstanceOf(CdCommentSearchEmptyException.class);
  }

  @Test
  @DisplayName("댓글 삭제 성공")
  void deleteComment_Success() {
    // given
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(cdComment));

    // when
    cdCommentService.deleteComment(1L);

    // then
    verify(cdCommentRepository, times(1)).delete(cdComment);
  }

  @Test
  @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
  void deleteComment_NotFound() {
    // given
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> cdCommentService.deleteComment(1L))
        .isInstanceOf(CdCommentNotFoundException.class);
  }

  @Test
  @DisplayName("다중 댓글 삭제 성공")
  void deleteMultipleComments_Success() {
    // given
    List<Long> commentIds = List.of(1L, 2L, 3L);
    List<CdComment> comments = List.of(cdComment, cdComment, cdComment);

    when(cdCommentRepository.findAllById(commentIds)).thenReturn(comments);

    // when
    cdCommentService.deleteMultipleComments(commentIds);

    // then
    verify(cdCommentRepository, times(1)).deleteAll(comments);
  }

  @Test
  @DisplayName("삭제할 댓글이 존재하지 않을 때 예외 발생")
  void deleteMultipleComments_NotFound() {
    // given
    when(cdCommentRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of());

    // when & then
    assertThatThrownBy(() -> cdCommentService.deleteMultipleComments(List.of(1L, 2L, 3L)))
        .isInstanceOf(CdCommentNotFoundException.class);
  }
}
