package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyCdRepository extends JpaRepository<MyCd, Long>, MyCdQueryRepository {

  boolean existsById(Long id);

  List<MyCd> findByUserId(Long userId);

  @Query("SELECT mc FROM MyCd mc WHERE mc.id = :myCdId AND mc.user.id = :userId")
  Optional<MyCd> findByIdAndUserIdOptimized(@Param("myCdId") Long myCdId,
      @Param("userId") Long userId);

  Page<MyCd> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);

  Page<MyCd> findByUserIdAndIdGreaterThanOrderByIdAsc(Long userId, Long id, Pageable pageable);

  Optional<MyCd> findByUserIdAndCdId(Long userId, Long cdId);

  long countByUserId(Long userId);

  Optional<MyCd> findFirstByUserIdOrderByIdAsc(Long userId);

  Optional<MyCd> findFirstByUserIdOrderByIdDesc(Long userId);

  @Modifying
  @Query("DELETE FROM MyCd mc WHERE mc.user.id = :userId AND mc.id IN (:ids)")
  void deleteByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
