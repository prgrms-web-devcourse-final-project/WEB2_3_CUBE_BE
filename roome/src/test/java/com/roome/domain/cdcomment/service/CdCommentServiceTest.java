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
import com.roome.global.exception.UnauthorizedException;
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

@ExtendWith(MockitoExtension.class)
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
    CdCommentCreateRequest request = new CdCommentCreateRequest("03:40", "이 곡 최고네요!");

    when(myCdRepository.findById(1L)).thenReturn(Optional.of(myCd));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(cdCommentRepository.save(any(CdComment.class))).thenReturn(cdComment);

    CdCommentResponse response = cdCommentService.addComment(1L, 1L, request);

    assertThat(response.getContent()).isEqualTo(request.getContent());
    assertThat(response.getTimestamp()).isEqualTo(request.getTimestamp());
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void getComments_Success() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> page = new PageImpl<>(List.of(cdComment), pageable, 1);

    when(cdCommentRepository.findByMyCdId(1L, pageable)).thenReturn(page);

    CdCommentListResponse response = cdCommentService.getComments(1L, 0, 5);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("CD 전체 댓글 목록 조회 성공")
  void getAllComments_Success() {
    List<CdComment> comments = List.of(
        cdComment,
        CdComment.builder()
            .id(2L)
            .user(user)
            .myCd(myCd)
            .timestamp("04:20")
            .content("이 곡도 좋아요!")
            .build()
    );

    when(cdCommentRepository.findByMyCdId(1L)).thenReturn(comments);

    List<CdCommentResponse> response = cdCommentService.getAllComments(1L);

    assertThat(response).hasSize(2);
    assertThat(response.get(0).getContent()).isEqualTo("이 곡 최고네요!");
    assertThat(response.get(1).getContent()).isEqualTo("이 곡도 좋아요!");
  }

  @Test
  @DisplayName("CD 전체 댓글 목록이 비어있을 때 예외 발생")
  void getAllComments_EmptyList() {
    // 빈 리스트 반환 설정
    doReturn(List.of()).when(cdCommentRepository).findByMyCdId(1L);

    // 예외 발생 검증
    assertThatThrownBy(() -> cdCommentService.getAllComments(1L))
        .isInstanceOf(CdCommentListEmptyException.class);
  }

  @Test
  @DisplayName("댓글 목록이 비어있을 때 예외 발생")
  void getComments_EmptyList() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> emptyPage = Page.empty(pageable);

    when(cdCommentRepository.findByMyCdId(1L, pageable)).thenReturn(emptyPage);

    assertThatThrownBy(() -> cdCommentService.getComments(1L, 0, 5))
        .isInstanceOf(CdCommentListEmptyException.class);
  }

  @Test
  @DisplayName("댓글 검색 성공")
  void searchComments_Success() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> page = new PageImpl<>(List.of(cdComment), pageable, 1);

    when(cdCommentRepository.findByMyCdIdAndKeyword(1L, "최고", pageable))
        .thenReturn(page);

    CdCommentListResponse response = cdCommentService.searchComments(1L, "최고", 0, 5);

    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).getContent()).isEqualTo("이 곡 최고네요!");
  }

  @Test
  @DisplayName("댓글 검색 결과가 없을 때 예외 발생")
  void searchComments_EmptyResult() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<CdComment> emptyPage = Page.empty(pageable);

    when(cdCommentRepository.findByMyCdIdAndKeyword(1L, "없는내용", pageable))
        .thenReturn(emptyPage);

    assertThatThrownBy(() -> cdCommentService.searchComments(1L, "없는내용", 0, 5))
        .isInstanceOf(CdCommentSearchEmptyException.class);
  }

  @Test
  @DisplayName("댓글 삭제 성공")
  void deleteComment_Success() {
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(cdComment));

    cdCommentService.deleteComment(1L, 1L);

    verify(cdCommentRepository, times(1)).delete(any(CdComment.class));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
  void deleteComment_NotFound() {
    when(cdCommentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cdCommentService.deleteComment(1L, 1L))
        .isInstanceOf(CdCommentNotFoundException.class);
  }

  @Test
  @DisplayName("다중 댓글 삭제 성공")
  void deleteMultipleComments_Success() {
    List<Long> commentIds = List.of(1L, 2L, 3L);
    List<CdComment> comments = List.of(cdComment, cdComment, cdComment);

    when(cdCommentRepository.findAllById(commentIds)).thenReturn(comments);

    cdCommentService.deleteMultipleComments(1L, commentIds);

    verify(cdCommentRepository, times(1)).deleteAll(anyList());
  }

  @Test
  @DisplayName("삭제할 댓글이 존재하지 않을 때 예외 발생")
  void deleteMultipleComments_NotFound() {
    when(cdCommentRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of());

    assertThatThrownBy(() -> cdCommentService.deleteMultipleComments(1L, List.of(1L, 2L, 3L)))
        .isInstanceOf(CdCommentNotFoundException.class);
  }

  @Test
  @DisplayName("다른 사용자의 댓글 삭제 시 UnauthorizedException 발생")
  void deleteComment_Unauthorized() {
    User anotherUser = User.builder()
        .id(2L)
        .nickname("다른 사용자")
        .build();

    CdComment anotherComment = CdComment.builder()
        .id(1L)
        .user(anotherUser)
        .myCd(myCd)
        .timestamp("03:40")
        .content("이 곡 최고네요!")
        .build();

    when(cdCommentRepository.findById(1L)).thenReturn(Optional.of(anotherComment));

    assertThatThrownBy(() -> cdCommentService.deleteComment(1L, 1L))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("다른 사용자의 댓글 다중 삭제 시 UnauthorizedException 발생")
  void deleteMultipleComments_Unauthorized() {
    User anotherUser = User.builder()
        .id(2L)
        .nickname("다른 사용자")
        .build();

    List<CdComment> anotherUserComments = List.of(
        CdComment.builder().id(1L).user(anotherUser).myCd(myCd).timestamp("03:40").content("이 곡 최고네요!").build(),
        CdComment.builder().id(2L).user(anotherUser).myCd(myCd).timestamp("02:20").content("좋은 곡!").build()
    );

    when(cdCommentRepository.findAllById(List.of(1L, 2L))).thenReturn(anotherUserComments);

    assertThatThrownBy(() -> cdCommentService.deleteMultipleComments(1L, List.of(1L, 2L)))
        .isInstanceOf(UnauthorizedException.class);
  }
}
