package com.roome.domain.cdcomment.service;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.exception.CdCommentListEmptyException;
import com.roome.domain.cdcomment.exception.CdCommentNotFoundException;
import com.roome.domain.cdcomment.exception.CdCommentSearchEmptyException;
import com.roome.domain.cdcomment.notificationEvent.CdCommentCreatedEvent;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.ForbiddenException;
import com.roome.global.exception.UnauthorizedException;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CdCommentService {

  private final CdCommentRepository cdCommentRepository;
  private final MyCdRepository myCdRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;// 이벤트 발행자

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

    // CD 소유자가 댓글 작성자와 다른 경우에만 알림 발생
    Long cdOwnerId = myCd.getUser().getId();
    if (!userId.equals(cdOwnerId)) {
      log.info("CD 코멘트 알림 이벤트 발행: 발신자={}, 수신자={}, CD={}, 코멘트={}",
          userId, cdOwnerId, myCdId, savedComment.getId());

      try {
        eventPublisher.publishEvent(new CdCommentCreatedEvent(
            this,
            userId,         // 발신자 (댓글 작성자)
            cdOwnerId,      // 수신자 (CD 소유자)
            myCdId,         // CD ID
            savedComment.getId() // 코멘트 ID
        ));
      } catch (Exception e) {
        log.error("CD 코멘트 알림 이벤트 발행 중 오류 발생: {}", e.getMessage(), e);
        // 알림 발행 실패가 비즈니스 로직에 영향을 주지 않도록 예외를 잡아서 처리
      }
    }

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

  public List<CdCommentResponse> getAllComments(Long myCdId) {
    List<CdComment> comments = cdCommentRepository.findByMyCdId(myCdId);

    if (comments.isEmpty()) {
      throw new CdCommentListEmptyException();
    }

    return comments.stream()
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
  public void deleteComment(Long userId, Long commentId) {
    CdComment comment = cdCommentRepository.findById(commentId)
        .orElseThrow(CdCommentNotFoundException::new);

    Long roomOwnerId = comment.getMyCd().getUser().getId();

    // 댓글 작성자 또는 방 주인만 삭제 가능
    if (!comment.getUser().getId().equals(userId) && !roomOwnerId.equals(userId)) {
      throw new ForbiddenException("해당 댓글을 삭제할 권한이 없습니다.");
    }

    cdCommentRepository.delete(comment);
  }


}
