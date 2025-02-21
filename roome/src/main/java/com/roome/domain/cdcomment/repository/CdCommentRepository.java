package com.roome.domain.cdcomment.repository;

import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdCommentRepository extends JpaRepository<CdComment, Long> {

  Long countByUserId(Long userId);

  List<CdComment> findByMyCdId(Long myCdId);

}