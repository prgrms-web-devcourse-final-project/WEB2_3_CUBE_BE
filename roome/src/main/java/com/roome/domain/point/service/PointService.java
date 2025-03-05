package com.roome.domain.point.service;

import com.roome.domain.point.dto.PointBalanceResponse;
import com.roome.domain.point.dto.PointHistoryDto;
import com.roome.domain.point.dto.PointHistoryResponse;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.exception.InsufficientPointsException;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.domain.point.exception.DuplicatePointEarnException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

  public void earnPoints(User user, PointReason reason) {
    int amount = POINT_EARN_MAP.getOrDefault(reason, 0);
    log.info("earnPoints - User: {}, Reason: {}, Amount: {}", user.getId(), reason, amount);

    if (pointHistoryRepository.existsRecentEarned(user.getId(), reason)) {
      log.warn("earnPoints - 중복 적립 시도! User: {}, Reason: {}", user.getId(), reason);
      throw new DuplicatePointEarnException();
    }

    // 포인트가 없으면 자동 생성하도록 수정
    Point point = pointRepository.findByUserId(user.getId())
        .orElseGet(() -> {
          log.info("earnPoints - 포인트 계정 없음, 새로 생성! User: {}", user.getId());
          return pointRepository.save(new Point(user, 0, 0, 0));
        });

    point.addPoints(amount);
    log.info("earnPoints - 포인트 적립 완료! User: {}, New Balance: {}", user.getId(), point.getBalance());

    savePointHistory(user, amount, reason);
  }


  public void usePoints(User user, PointReason reason) {
    int amount = POINT_USAGE_MAP.getOrDefault(reason, 0);
    log.info("usePoints - User: {}, Reason: {}, Amount: {}", user.getId(), reason, amount);

    // 포인트가 없으면 자동 생성하도록 수정
    Point point = pointRepository.findByUserId(user.getId())
        .orElseGet(() -> {
          log.info("usePoints - 포인트 계정 없음, 새로 생성! User: {}", user.getId());
          return pointRepository.save(new Point(user, 0, 0, 0));
        });

    if (point.getBalance() < amount) {
      log.warn("usePoints - 포인트 부족! User: {}, Balance: {}, Required: {}", user.getId(),
          point.getBalance(), amount);
      throw new InsufficientPointsException();
    }

    point.subtractPoints(amount);
    log.info("usePoints - 포인트 사용 완료! User: {}, New Balance: {}", user.getId(), point.getBalance());

    savePointHistory(user, -amount, reason);
  }

  private void savePointHistory(User user, int amount, PointReason reason) {
    log.info("savePointHistory - User: {}, Amount: {}, Reason: {}", user.getId(), amount, reason);
    pointHistoryRepository.save(new PointHistory(user, amount, reason));
  }

  @Transactional(readOnly = true)
  public PointHistoryResponse getPointHistory(Long userId, Long cursor, int size) {
    log.info("getPointHistory - User: {}, Cursor: {}, Size: {}", userId, cursor, size);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("getPointHistory - 사용자 없음! UserId: {}", userId);
      return new UserNotFoundException();
    });

    int balance = pointRepository.findByUserId(user.getId()).map(Point::getBalance).orElse(0);
    Pageable pageable = PageRequest.of(0, size);
    Slice<PointHistory> historySlice = cursor == 0 ?
        pointHistoryRepository.findByUserOrderByIdDesc(user, pageable) :
        pointHistoryRepository.findByUserAndIdLessThanOrderByIdDesc(user, cursor, pageable);

    List<PointHistoryDto> historyItems = historySlice.getContent().stream()
        .map(PointHistoryDto::fromEntity)
        .collect(Collectors.toList());

    log.info("getPointHistory - 조회 완료! User: {}, History Count: {}", userId, historyItems.size());

    return PointHistoryResponse.fromEntityList(historyItems, balance,
        pointHistoryRepository.countByUserId(user.getId()),
        historyItems.isEmpty() ? null : historyItems.get(0).getId(),
        historyItems.isEmpty() ? null : historyItems.get(historyItems.size() - 1).getId(),
        historySlice.hasNext() ? historyItems.get(historyItems.size() - 1).getId() : null);
  }

  @Transactional(readOnly = true)
  public PointBalanceResponse getMyPointBalance(Long userId) {
    log.info("getMyPointBalance - User: {}", userId);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("getMyPointBalance - 사용자 없음! UserId: {}", userId);
      return new UserNotFoundException();
    });

    int balance = pointRepository.findByUserId(user.getId()).map(Point::getBalance).orElse(0);

    log.info("getMyPointBalance - User: {}, Balance: {}", userId, balance);
    return new PointBalanceResponse(balance);
  }
}
