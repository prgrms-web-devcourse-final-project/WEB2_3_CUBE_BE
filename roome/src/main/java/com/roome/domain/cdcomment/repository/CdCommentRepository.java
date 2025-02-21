package com.roome.domain.cdcomment.repository;

import com.roome.domain.cdcomment.entity.CdComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdCommentRepository extends JpaRepository<CdComment, Long> {

  Long countByUserId(Long userId);


  Page<CdComment> findByMyCdId(Long myCdId, PageRequest pageRequest);

}