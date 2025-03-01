package com.roome.domain.houseMate.repository;

import com.roome.domain.houseMate.entity.AddedHousemate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousemateRepository extends JpaRepository<AddedHousemate, Long>, HousemateRepositoryCustom {
    // 양방향 체크도 하나의 테이블에서 가능
    boolean existsByUserIdAndAddedId(Long userId, Long targetId);
    // 삭제도 단순화
    void deleteByUserIdAndAddedId(Long userId, Long targetId);
    // 사용자가 추가한 하우스메이트 관계와 사용자를 추가한 하우스메이트 관계 모두 삭제
    void deleteByUserIdOrAddedId(Long userId, Long targetId);
}