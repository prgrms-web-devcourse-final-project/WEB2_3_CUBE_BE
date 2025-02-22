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
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CdCommentService {

  private final CdCommentRepository cdCommentRepository;
  private final MyCdRepository myCdRepository;
  private final UserRepository userRepository;

  public CdCommentResponse addComment(Long userId, Long myCdId, CdCommentCreateRequest request) {
    MyCd myCd = myCdRepository.findById(myCdId)
        .orElseThrow(MyCdNotFoundException::new);

    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    CdComment comment = CdComment.builder()
        .myCd(myCd)
        .user(user)
        .timestamp(request.getTimestamp())
        .content(request.getContent())
        .build();

    CdComment savedComment = cdCommentRepository.save(comment);

    return new CdCommentResponse(
        savedComment.getId(),
        savedComment.getMyCd().getId(),
        savedComment.getUser().getId(),
        savedComment.getUser().getNickname(),
        savedComment.getTimestamp(),
        savedComment.getContent(),
        savedComment.getCreatedAt()
    );
  }

  @Transactional
  public CdCommentListResponse getComments(Long myCdId, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<CdComment> commentPage = cdCommentRepository.findByMyCdId(myCdId, pageRequest);

    if (commentPage.isEmpty()) {
      throw new CdCommentListEmptyException();
    }

    List<CdCommentResponse> comments = commentPage.getContent().stream()
        .map(comment -> new CdCommentResponse(
            comment.getId(),
            comment.getMyCd().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getTimestamp(),
            comment.getContent(),
            comment.getCreatedAt()
        ))
        .collect(Collectors.toList());

    return new CdCommentListResponse(comments, page, size, commentPage.getTotalElements(),
        commentPage.getTotalPages());
  }

  public CdCommentListResponse searchComments(Long myCdId, String keyword, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<CdComment> commentPage = cdCommentRepository.findByMyCdIdAndKeyword(myCdId, keyword,
        pageable);

    if (commentPage.isEmpty()) {
      throw new CdCommentSearchEmptyException();
    }

    return new CdCommentListResponse(
        commentPage.map(comment -> new CdCommentResponse(
            comment.getId(),
            comment.getMyCd().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getTimestamp(),
            comment.getContent(),
            comment.getCreatedAt()
        )).toList(),
        page,
        size,
        commentPage.getTotalElements(),
        commentPage.getTotalPages()
    );
  }

  @Transactional
  public void deleteComment(Long commentId) {
    CdComment comment = cdCommentRepository.findById(commentId)
        .orElseThrow(CdCommentNotFoundException::new);

    cdCommentRepository.delete(comment);
  }

  @Transactional
  public void deleteMultipleComments(List<Long> commentIds) {
    List<CdComment> comments = cdCommentRepository.findAllById(commentIds);

    if (comments.isEmpty()) {
      throw new CdCommentNotFoundException();
    }

    cdCommentRepository.deleteAll(comments);
  }
}
