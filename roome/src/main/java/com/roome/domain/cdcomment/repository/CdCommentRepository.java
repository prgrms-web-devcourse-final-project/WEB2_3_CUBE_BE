package com.roome.domain.cdcomment.repository;

import com.roome.domain.cdcomment.entity.CdComment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CdCommentRepository extends JpaRepository<CdComment, Long> {

  Long countByUserId(Long userId);

  @Query("SELECT c FROM CdComment c WHERE c.myCd.id = :myCdId")
  Page<CdComment> findByMyCdId(@Param("myCdId") Long myCdId, Pageable pageable);

  List<CdComment> findByMyCdId(Long myCdId);

  @Query(value = "SELECT * FROM cd_comment c WHERE c.my_cd_id = :myCdId AND c.content COLLATE utf8mb4_general_ci LIKE CONCAT('%', :keyword, '%')", nativeQuery = true)
  Page<CdComment> findByMyCdIdAndKeyword(@Param("myCdId") Long myCdId,
      @Param("keyword") String keyword, Pageable pageable);


  // 특정 사용자가 작성한 모든 CD 댓글 목록 조회
  List<CdComment> findAllByUserId(Long userId);

  // 특정 myCdId 리스트로 댓글 조회 및 삭제
  List<CdComment> findByMyCdIdIn(List<Long> myCdIds);
}