package com.roome.domain.mate.repository;

import com.roome.domain.mate.dto.HousemateInfo;
import com.roome.domain.mate.entity.AddedHousemate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface AddedHousemateRepository extends JpaRepository<AddedHousemate, Long> {
    // 양방향 체크도 하나의 테이블에서 가능
    boolean existsByUserIdAndAddedId(Long userId, Long addedId);
    // 삭제도 단순화
    void deleteByUserIdAndAddedId(Long userId, Long addedId);
}