package com.roome.domain.houseMate.repository;

import com.roome.domain.houseMate.entity.AddedHousemate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousemateRepository extends JpaRepository<AddedHousemate, Long>, HousemateRepositoryCustom {
    // 양방향 체크도 하나의 테이블에서 가능
    boolean existsByUserIdAndAddedId(Long userId, Long addedId);
    // 삭제도 단순화
    void deleteByUserIdAndAddedId(Long userId, Long addedId);
}