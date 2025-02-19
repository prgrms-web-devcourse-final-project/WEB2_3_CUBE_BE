package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyCdRepository extends JpaRepository<MyCd, Long> {

  boolean existsByUserIdAndCdId(Long userId, Long cdId);

  List<MyCd> findByUserId(Long userId);
}