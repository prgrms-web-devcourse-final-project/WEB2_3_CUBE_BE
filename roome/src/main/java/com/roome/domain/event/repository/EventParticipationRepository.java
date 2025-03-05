package com.roome.domain.event.repository;

import com.roome.domain.event.entity.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {

  long countByEventId(Long eventId); // 특정 이벤트의 참여자 수 조회

  boolean existsByUserIdAndEventId(Long userId, Long eventId); // 특정 유저의 참여 여부 확인
}
