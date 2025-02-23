package com.roome.domain.cdcomment.repository;

import com.roome.domain.cdcomment.entity.CdComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CdCommentRepository extends JpaRepository<CdComment, Long> {

  Long countByUserId(Long userId);

  Page<CdComment> findByMyCdId(Long myCdId, Pageable pageable);

  @Query("SELECT c FROM CdComment c " + "WHERE c.myCd.id = :myCdId "
      + "AND (LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) "
      + "OR LOWER(c.user.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<CdComment> findByMyCdIdAndKeyword(@Param("myCdId") Long myCdId,
      @Param("keyword") String keyword, Pageable pageable);

  // 특정 사용자가 작성한 모든 CD 댓글 목록 조회
  List<CdComment> findAllByUserId(Long userId);
}