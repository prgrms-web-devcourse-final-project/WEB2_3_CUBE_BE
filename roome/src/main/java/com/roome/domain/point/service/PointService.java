package com.roome.domain.point.service;

import com.roome.domain.point.dto.PointBalanceResponse;
import com.roome.domain.point.dto.PointHistoryDto;
import com.roome.domain.point.dto.PointHistoryGroupedDto;
import com.roome.domain.point.dto.PointHistoryResponse;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.event.PointEvent;
import com.roome.domain.point.exception.InsufficientPointsException;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.service.RedisLockService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
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
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockService redisLockService; // 추가
  private final ApplicationEventPublisher eventPublisher; // 이벤트 게시자 추가


  private static final String BALANCE_CACHE_PREFIX = "point_balance:";

  private static final Duration CACHE_DURATION = Duration.ofMinutes(10); // 캐싱 유지 시간

  private static final Map<PointReason, Integer> POINT_EARN_MAP = Map.of(
      PointReason.GUESTBOOK_REWARD, 10,
      PointReason.FIRST_COME_EVENT, 200,
      PointReason.DAILY_ATTENDANCE, 400,
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
      PointReason.CD_UNLOCK_LV3, 1500,
      PointReason.POINT_REFUND_100, 100,
      PointReason.POINT_REFUND_550, 550,
      PointReason.POINT_REFUND_1200, 1200,
      PointReason.POINT_REFUND_4000, 4000
  );

  public void earnPoints(User user, PointReason reason) {
    String lockKey = "lock:point:user:" + user.getId();

    redisLockService.executeWithLock(lockKey, 5, 2, () -> {
      earnPointsInternal(user, reason);
      return null;
    });
  }

  @Transactional
  public void earnPointsInternal(User user, PointReason reason) {
    Point point = pointRepository.findByUserId(user.getId())
        .orElseGet(() -> pointRepository.save(new Point(user, 0, 0, 0)));

    int amount = POINT_EARN_MAP.getOrDefault(reason, 0);
    point.addPoints(amount);
    savePointHistory(user, amount, reason);

    // 포인트 적립 이벤트 발생
    publishPointEarnedEvent(user, point.getId(), amount, reason);

    redisTemplate.delete(BALANCE_CACHE_PREFIX + user.getId()); // 캐시 삭제 추가
  }

  @Transactional
  protected void usePointsInternal(User user, PointReason reason) {
    int amount = POINT_USAGE_MAP.getOrDefault(reason, 0);
    log.info("usePoints - User: {}, Reason: {}, Amount: {}", user.getId(), reason, amount);

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
    redisTemplate.delete(BALANCE_CACHE_PREFIX + user.getId());
  }

  public void usePoints(User user, PointReason reason) {
    String lockKey = "lock:point:user:" + user.getId();

    redisLockService.executeWithLock(lockKey, 5, 2, () -> {
      usePointsInternal(user, reason);
      return null;
    });
  }

  private void savePointHistory(User user, int amount, PointReason reason) {
    log.info("savePointHistory - User: {}, Amount: {}, Reason: {}", user.getId(), amount, reason);
    pointHistoryRepository.save(new PointHistory(user, amount, reason));
  }

  @Transactional(readOnly = true)
  public PointHistoryResponse getPointHistory(Long userId, String dayCursor, Long itemCursor,
      int size) {
    log.info("getPointHistory - User: {}, DayCursor: {}, ItemCursor: {}, Size: {}", userId,
        dayCursor, itemCursor, size);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("getPointHistory - 사용자 없음! UserId: {}", userId);
      return new UserNotFoundException();
    });

    int balance = pointRepository.findByUserId(user.getId()).map(Point::getBalance).orElse(0);
    Pageable pageable = PageRequest.of(0, size);

    // 전체 데이터 기준 firstId, lastId 조회
    Long firstId = pointHistoryRepository.findFirstIdByUser(userId);
    Long lastId = pointHistoryRepository.findLastIdByUser(userId);

    Slice<PointHistory> historySlice;

    if (dayCursor == null || itemCursor == null) {
      // 처음 요청일 경우 (최신순)
      historySlice = pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId(),
          pageable);
    } else {
      // 커서가 있을 경우 (페이징 처리)
      LocalDate parsedDayCursor = LocalDate.parse(dayCursor);
      historySlice = pointHistoryRepository.findHistoryByCursor(user.getId(), parsedDayCursor,
          itemCursor, pageable);
    }

    List<PointHistory> historyList = historySlice.getContent();

    if (historyList.isEmpty()) {
      return new PointHistoryResponse(Collections.emptyList(), balance, 0, null, null, null, null);
    }

    // 날짜별 그룹핑
    Map<LocalDate, List<PointHistoryDto>> groupedHistory = historyList.stream()
        .map(PointHistoryDto::fromEntity)
        .collect(Collectors.groupingBy(dto -> LocalDate.parse(dto.getDateTime().split(" ")[0]),
            LinkedHashMap::new, Collectors.toList()));

    List<PointHistoryGroupedDto> groupedResponse = groupedHistory.entrySet().stream()
        .map(entry -> new PointHistoryGroupedDto(entry.getKey().toString(), entry.getValue()))
        .collect(Collectors.toList());

    // 다음 커서 계산
    String nextDayCursor = null;
    Long nextItemCursor = null;

    if (!groupedResponse.isEmpty()) {
      PointHistoryGroupedDto lastGroup = groupedResponse.get(groupedResponse.size() - 1);
      nextDayCursor = lastGroup.getDate();

      List<PointHistoryDto> lastItems = lastGroup.getItems();
      if (!lastItems.isEmpty()) {
        nextItemCursor = lastItems.get(lastItems.size() - 1).getId();
      }
    }

    log.info("getPointHistory - 조회 완료! User: {}, NextDayCursor: {}, NextItemCursor: {}", userId,
        nextDayCursor, nextItemCursor);

    return new PointHistoryResponse(
        groupedResponse, balance, pointHistoryRepository.countByUserId(user.getId()),
        firstId, lastId, nextDayCursor, nextItemCursor
    );
  }


  public PointBalanceResponse getUserPointBalance(Long userId) {
    log.info("getUserPointBalance - User: {}", userId);
    String cacheKey = BALANCE_CACHE_PREFIX + userId;

    // 1. Redis에서 캐싱된 포인트 조회
    Integer cachedBalance = (Integer) redisTemplate.opsForValue().get(cacheKey);
    if (cachedBalance != null) {
      log.info("getUserPointBalance - 캐싱된 데이터 반환, User: {}, Balance: {}", userId, cachedBalance);
      return new PointBalanceResponse(cachedBalance);
    }

    // 2. 캐싱된 값이 없으면 DB 조회
    int balance = pointRepository.findByUserId(userId)
        .map(Point::getBalance)
        .orElse(0);

    log.info("getUserPointBalance - DB 조회 후 캐싱, User: {}, Balance: {}", userId, balance);

    // 3. Redis 캐시 비동기 업데이트
    updateCache(userId, balance);

    return new PointBalanceResponse(balance);
  }

  public void updateCache(Long userId, int balance) {
    log.info("updateCache - Redis 캐싱 업데이트 시작, User: {}", userId);
    redisTemplate.opsForValue().set(BALANCE_CACHE_PREFIX + userId, balance, CACHE_DURATION);
    log.info("updateCache - Redis 캐싱 업데이트 완료, User: {}", userId);
  }

  private void publishPointEarnedEvent(User user, Long pointId, int amount, PointReason reason) {
    log.info("publishPointEarnedEvent - User: {}, Amount: {}, Reason: {}", user.getId(), amount,
        reason);

    // 이벤트 발행 (시스템에서 사용자에게 알림 전송, senderId는 시스템 ID인 0L로 설정)
    eventPublisher.publishEvent(new PointEvent(
        this,       // 이벤트 소스
        0L,         // 시스템 ID (sender로 시스템을 사용)
        user.getId(), // 수신자 ID
        pointId,    // 대상 ID (Point ID)
        amount,     // 적립 금액
        reason      // 적립 사유
    ));
  }

}
