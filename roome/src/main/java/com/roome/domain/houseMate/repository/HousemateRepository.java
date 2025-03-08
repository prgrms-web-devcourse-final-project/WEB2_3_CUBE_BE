package com.roome.domain.houseMate.repository;

import com.roome.domain.houseMate.entity.AddedHousemate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HousemateRepository extends JpaRepository<AddedHousemate, Long>,
    HousemateRepositoryCustom {

  // 양방향 체크도 하나의 테이블에서 가능
  boolean existsByUserIdAndAddedId(Long userId, Long targetId);

  // 삭제도 단순화
  void deleteByUserIdAndAddedId(Long userId, Long targetId);

  // 사용자가 추가한 하우스메이트 관계와 사용자를 추가한 하우스메이트 관계 모두 삭제
  int deleteByUserIdOrAddedId(Long userId, Long targetId);

  /// 사용자가 하우스메이트로 추가한 사용자 ID 목록 조회
  /// @param userId 사용자 ID
  /// @return 하우스메이트로 추가한 사용자 ID 목록
  @Query("SELECT h.addedId FROM AddedHousemate h WHERE h.userId = :userId")
  List<Long> findAddedIdsByUserId(@Param("userId") Long userId);
}