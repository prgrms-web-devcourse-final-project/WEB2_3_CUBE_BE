package com.roome.domain.point.service;

import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.exception.DuplicatePointEarnException;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

  private final PointRepository pointRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final UserRepository userRepository;

  // 포인트 적립 (PointReason 기반)
  public void earnPoints(Long userId, PointReason reason) {
    int amount = getPointAmount(reason); // PointReason에 따른 포인트 금액 결정

    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    // 하루 1회 제한이 필요한 포인트 적립 방지
    if (isDuplicateEarn(userId, reason)) {
      throw new DuplicatePointEarnException();
    }

    // 포인트 적립 로직
    Point point = pointRepository.findByUser(user)
        .orElseGet(() -> pointRepository.save(new Point(user, 0, 0, 0)));

    point.addPoints(amount);
    pointRepository.save(point);

    // 포인트 적립 내역 저장
    pointHistoryRepository.save(new PointHistory(user, amount, reason));
  }

  // 특정 유저가 하루에 한 번만 적립 가능한 포인트인지 확인
  private boolean isDuplicateEarn(Long userId, PointReason reason) {
    return reason == PointReason.GUESTBOOK_REWARD || reason == PointReason.DAILY_ATTENDANCE
        ? pointHistoryRepository.existsByUserIdAndReasonAndCreatedAt(userId, reason, LocalDate.now())
        : false;
  }

  // PointReason에 따라 적립되는 포인트 금액을 반환
  private int getPointAmount(PointReason reason) {
    return switch (reason) {
      case GUESTBOOK_REWARD -> 10;
      case FIRST_COME_EVENT -> 200;
      case DAILY_ATTENDANCE -> (int) (Math.random() * (100 - 50 + 1) + 50); // 랜덤 50~100P 지급
      case POINT_PURCHASE_100 -> 100;
      case POINT_PURCHASE_550 -> 550;
      case POINT_PURCHASE_1200 -> 1200;
      case POINT_PURCHASE_4000 -> 4000;
      default -> throw new IllegalArgumentException("유효하지 않은 포인트 적립 사유입니다.");
    };
  }
}
