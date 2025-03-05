package com.roome.domain.event.repository;

import com.roome.domain.event.entity.FirstComeEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirstComeEventRepository extends JpaRepository<FirstComeEvent, Long> {

  Optional<FirstComeEvent> findTopByOrderByEventTimeDesc(); // 가장 최근 이벤트 조회
}

