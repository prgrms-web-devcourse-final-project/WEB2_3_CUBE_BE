package com.roome.domain.rank.service;

import com.roome.domain.rank.entity.ScoreUpdateTask;
import com.roome.domain.rank.entity.TaskStatus;
import com.roome.domain.rank.repository.ScoreUpdateTaskRepository;
import com.roome.global.service.RedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreRecoveryService {

  private final ScoreUpdateTaskRepository scoreUpdateTaskRepository;
  private final RedisService redisService;

  @Scheduled(fixedRate = 60000)  // 1분마다 실행
  @Transactional
  public void recoverFailedScoreUpdates() {
    List<ScoreUpdateTask> failedTasks = scoreUpdateTaskRepository.findByStatusOrderByCreatedAtAsc(
        TaskStatus.FAILED);

    for (ScoreUpdateTask task : failedTasks) {
      try {
        redisService.incrementUserScoreWithLock(task.getUserId(), task.getScore());
        task.setStatus(TaskStatus.COMPLETED);
      } catch (Exception e) {
        task.incrementRetryCount();
        if (task.getRetryCount() > 5) {
          task.setStatus(TaskStatus.ABANDONED);
        }
        log.warn("점수 업데이트 복구 실패: taskId={}, userId={}, retryCount={}",
            task.getId(), task.getUserId(), task.getRetryCount());
      }
      scoreUpdateTaskRepository.save(task);
    }
  }
}
