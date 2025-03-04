package com.roome.domain.point.service;

import com.roome.domain.point.dto.PointBalanceResponse;
import com.roome.domain.point.dto.PointHistoryDto;
import com.roome.domain.point.dto.PointHistoryResponse;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.domain.point.exception.DuplicatePointEarnException;
import com.roome.domain.point.exception.InsufficientPointsException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

  private final PointRepository pointRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final UserRepository userRepository;

  private static final Map<PointReason, Integer> POINT_EARN_MAP = Map.of(
      PointReason.GUESTBOOK_REWARD, 10,
      PointReason.FIRST_COME_EVENT, 200,
      PointReason.DAILY_ATTENDANCE, 50,
      PointReason.POINT_PURCHASE_100, 100,
      PointReason.POINT_PURCHASE_550, 550,
      PointReason.POINT_PURCHASE_1200, 1200,
      PointReason.POINT_PURCHASE_4000, 4000
  );

  private static final Map<PointReason, Integer> POINT_USAGE_MAP = Map.of(
      PointReason.THEME_PURCHASE, 400,
      PointReason.BOOK_UNLOCK_LV2, 500,
      PointReason.BOOK_UNLOCK_LV3, 1500,
      PointReason.CD_UNLOCK_LV2, 500,
      PointReason.CD_UNLOCK_LV3, 1500
  );

  public void earnPoints(Long userId, PointReason reason) {
    int amount = POINT_EARN_MAP.getOrDefault(reason, 0);
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    if (pointHistoryRepository.existsByUserIdAndReasonAndCreatedAt(userId, reason,
        LocalDate.now())) {
      throw new DuplicatePointEarnException();
    }

    Point point = pointRepository.findByUser(user)
        .orElseGet(() -> pointRepository.save(new Point(user, 0, 0, 0)));

    point.addPoints(amount);
    pointRepository.save(point);
    savePointHistory(user, amount, reason);
  }

  public void usePoints(Long userId, PointReason reason) {
    int amount = POINT_USAGE_MAP.getOrDefault(reason, 0);
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    Point point = pointRepository.findByUser(user).orElseThrow(InsufficientPointsException::new);

    if (point.getBalance() < amount) {
      throw new InsufficientPointsException();
    }

    point.subtractPoints(amount);
    pointRepository.save(point);
    savePointHistory(user, -amount, reason);
  }

  private void savePointHistory(User user, int amount, PointReason reason) {
    pointHistoryRepository.save(new PointHistory(user, amount, reason));
  }

  @Transactional(readOnly = true)
  public PointHistoryResponse getPointHistory(Long userId, Long cursor, int size) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    int balance = pointRepository.findByUser(user).map(Point::getBalance).orElse(0);
    Pageable pageable = PageRequest.of(0, size);
    Slice<PointHistory> historySlice = cursor == 0 ?
        pointHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable) :
        pointHistoryRepository.findByUserAndIdLessThanOrderByCreatedAtDesc(user, cursor, pageable);

    List<PointHistoryDto> historyItems = historySlice.getContent().stream()
        .map(PointHistoryDto::fromEntity)
        .collect(Collectors.toList());

    return PointHistoryResponse.fromEntityList(historyItems, balance,
        pointHistoryRepository.countByUserId(user.getId()),
        historyItems.isEmpty() ? null : historyItems.get(0).getId(),
        historyItems.isEmpty() ? null : historyItems.get(historyItems.size() - 1).getId(),
        historySlice.hasNext() ? historyItems.get(historyItems.size() - 1).getId() : null);
  }

  @Transactional(readOnly = true)
  public PointBalanceResponse getMyPointBalance(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    int balance = pointRepository.findByUser(user).map(Point::getBalance).orElse(0);
    return new PointBalanceResponse(balance);
  }
}
