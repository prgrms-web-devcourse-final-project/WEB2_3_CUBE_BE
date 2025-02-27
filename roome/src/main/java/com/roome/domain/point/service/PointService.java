package com.roome.domain.point.service;

import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

  private final PointRepository pointRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final UserRepository userRepository;

  private static final int GUESTBOOK_REWARD_POINTS = 10;

  public void addGuestbookReward(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    Point point = pointRepository.findByUser(user)
        .orElseGet(() -> pointRepository.save(new Point(user, 0, 0, 0)));

    LocalDateTime now = LocalDateTime.now();
    if (user.getLastGuestbookReward() != null && user.getLastGuestbookReward().toLocalDate()
        .isEqual(now.toLocalDate())) {
      return;
    }

    point.addPoints(GUESTBOOK_REWARD_POINTS);
    pointRepository.save(point);

    user.updateLastGuestbookReward(now);
    userRepository.save(user);

    pointHistoryRepository.save(
        new PointHistory(user, GUESTBOOK_REWARD_POINTS, PointReason.GUESTBOOK_REWARD));
  }
}
