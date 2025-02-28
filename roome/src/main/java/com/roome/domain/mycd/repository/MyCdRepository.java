package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyCdRepository extends JpaRepository<MyCd, Long> {

  boolean existsById(Long id);

  boolean existsByUserIdAndCdId(Long userId, Long cdId);

  List<MyCd> findByUserId(Long userId);

  Optional<MyCd> findByIdAndUserId(Long myCdId, Long userId);

  List<MyCd> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);

  List<MyCd> findByUserIdAndIdGreaterThanOrderByIdAsc(Long userId, Long id, Pageable pageable);

  long countByUserId(Long userId); // 사용자의 전체 MyCd 개수 반환

  // 키워드 기반 검색 (CD 제목 또는 가수명)
  @Query("SELECT mc FROM MyCd mc JOIN mc.cd c " +
      "WHERE mc.user.id = :userId " +
      "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(c.artist) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  List<MyCd> searchByUserIdAndKeyword(@Param("userId") Long userId,
      @Param("keyword") String keyword,
      Pageable pageable);

  // 검색된 결과의 전체 개수 반환
  @Query("SELECT COUNT(mc) FROM MyCd mc JOIN mc.cd c " +
      "WHERE mc.user.id = :userId " +
      "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(c.artist) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  long countByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

  @Modifying
  @Query("DELETE FROM MyCd mc WHERE mc.user.id = :userId AND mc.id IN (:ids)")
  void deleteByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
