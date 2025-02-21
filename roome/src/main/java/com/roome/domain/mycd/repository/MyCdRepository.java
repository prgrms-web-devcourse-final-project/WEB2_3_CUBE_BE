package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyCdRepository extends JpaRepository<MyCd, Long> {

  boolean existsById(Long id);

  boolean existsByUserIdAndCdId(Long userId, Long cdId);

  List<MyCd> findByUserId(Long userId);

  Optional<MyCd> findByIdAndUserId(Long myCdId, Long userId);

  @Modifying
  @Query("DELETE FROM MyCd mc WHERE mc.user.id = :userId AND mc.id IN (:ids)")
  void deleteByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

}