package com.roome.domain.event.repository;

import com.roome.domain.event.entity.EventStatus;
import com.roome.domain.event.entity.FirstComeEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirstComeEventRepository extends JpaRepository<FirstComeEvent, Long> {

  List<FirstComeEvent> findByStatus(EventStatus status);

  // 진행 중인 가장 최신 이벤트 조회
  Optional<FirstComeEvent> findTopByStatusOrderByEventTimeDesc(EventStatus status);

}

