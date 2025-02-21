package com.roome.domain.cdcomment.service;

import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CdCommentService {
  private final CdCommentRepository cdCommentRepository;
  private final MyCdRepository myCdRepository;
  private final UserRepository userRepository;

  public CdCommentResponse addComment(Long myCdId, Long userId, CdCommentCreateRequest request) {
    MyCd myCd = myCdRepository.findById(myCdId)
        .orElseThrow(() -> new EntityNotFoundException("MyCD not found"));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
        savedComment.getTimestamp(),
        savedComment.getContent(),
        savedComment.getCreatedAt()
    );
  }

  @Transactional
  public CdCommentListResponse getComments(Long myCdId, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<CdComment> commentPage = cdCommentRepository.findByMyCdId(myCdId, pageRequest);

    List<CdCommentResponse> comments = commentPage.getContent().stream()
        .map(comment -> new CdCommentResponse(
            comment.getId(),
            comment.getMyCd().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getContent(),
            comment.getCreatedAt()
        ))
        .collect(Collectors.toList());

    return new CdCommentListResponse(comments, page, size, commentPage.getTotalElements(), commentPage.getTotalPages());
  }
}

