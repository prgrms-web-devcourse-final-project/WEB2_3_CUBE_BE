package com.roome.domain.rank.repository;

import com.roome.domain.rank.entity.ScoreUpdateTask;
import com.roome.domain.rank.entity.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreUpdateTaskRepository extends JpaRepository<ScoreUpdateTask, Long> {

  List<ScoreUpdateTask> findByStatusOrderByCreatedAtAsc(TaskStatus status);

  // 특정 사용자, 점수, 상태에 해당하는 작업 찾기 (중복 체크용)
  ScoreUpdateTask findByUserIdAndScoreAndStatus(Long userId, int score, TaskStatus status);
}