package com.roome.domain.cdcomment.repository;

import com.roome.domain.cdcomment.entity.CdComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CdCommentRepository extends JpaRepository<CdComment, Long> {

  Long countByUserId(Long userId);

  Page<CdComment> findByMyCdId(Long myCdId, PageRequest pageRequest);

  @Query("SELECT c FROM CdComment c " + "WHERE c.myCd.id = :myCdId "
      + "AND (LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) "
      + "OR LOWER(c.user.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<CdComment> findByMyCdIdAndKeyword(@Param("myCdId") Long myCdId,
      @Param("keyword") String keyword, Pageable pageable);
}