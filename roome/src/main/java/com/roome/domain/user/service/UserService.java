package com.roome.domain.user.service;

import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.service.RedisService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final HousemateRepository housemateRepository;
  private final RoomRepository roomRepository;
  private final FurnitureRepository furnitureRepository;
  private final MyBookRepository myBookRepository;
  private final MyBookReviewRepository myBookReviewRepository;
  private final MyCdRepository myCdRepository;
  private final CdCommentRepository cdCommentRepository;
  private final GuestbookRepository guestbookRepository;
  private final MyCdCountRepository myCdCountRepository;
  private final MyBookCountRepository myBookCountRepository;
  private final PointRepository pointRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final RedisService redisService;

  @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    log.info("[회원탈퇴] 시작: userId={}", userId);

    // 1. 하우스메이트 관계 삭제
    deleteHousemateRelations(userId);

    // 2. 도서 관련 데이터 삭제
    deleteBookRelatedData(userId);

    // 3. CD 관련 데이터 삭제
    deleteCdRelatedData(userId);

    // 4. 포인트 및 포인트 내역 삭제
    deletePointData(userId);

    // 5. Room 관련 데이터 삭제
    Optional<Room> roomOpt = roomRepository.findByUserId(userId);
    if (roomOpt.isPresent()) {
      Room room = roomOpt.get();

      // 5-1. 방명록 삭제
      List<Guestbook> guestbooks = guestbookRepository.findAllByRoomOrUserId(room, userId);
      if (!guestbooks.isEmpty()) {
        guestbookRepository.deleteAll(guestbooks);
        log.debug("[회원탈퇴] 방명록 삭제 완료: {}개", guestbooks.size());
      }

      // 5-2. 가구 삭제
      List<Furniture> furnitures = furnitureRepository.findByRoomId(room.getId());
      if (!furnitures.isEmpty()) {
        for (Furniture furniture : furnitures) {
          furnitureRepository.delete(furniture);
        }
        log.debug("[회원탈퇴] 가구 삭제 완료: {}개", furnitures.size());
      }

      // User와 Room의 관계 끊기
      user.setRoom(null);
      userRepository.saveAndFlush(user);

      // 5-3. Room 삭제
      roomRepository.delete(room);
      log.debug("[회원탈퇴] 방 삭제 완료: roomId={}", room.getId());
    }

    // 6. Redis에서 랭킹 데이터 삭제
    deleteRedisRankingData(userId);

    // 7. 사용자 삭제
    userRepository.delete(user);
    log.info("[회원탈퇴] 사용자 삭제 완료: {}", userId);
  }

  // Redis에서 랭킹 관련 데이터 삭제
  private void deleteRedisRankingData(Long userId) {
    try {
      // Redis에서 사용자 랭킹 점수 삭제
      boolean removed = redisService.deleteUserRankingData(userId.toString());
      log.debug("[회원탈퇴] Redis 랭킹 데이터 삭제 완료: userId={}, 성공={}", userId, removed);
    } catch (Exception e) {
      // Redis 작업 실패는 기록만 하고 계속 진행
      log.warn("[회원탈퇴] Redis 랭킹 데이터 삭제 실패 (계속 진행): userId={}, 사유={}",
          userId, e.getMessage());
    }
  }

  private void deleteHousemateRelations(Long userId) {
    // 양방향 관계 모두 삭제 (내가 추가한 것 + 나를 추가한 것)
    int count = housemateRepository.deleteByUserIdOrAddedId(userId, userId);
    log.debug("[회원탈퇴] 하우스메이트 관계 삭제 수: {}", count);
  }

  private void deleteBookRelatedData(Long userId) {
    // 도서 리뷰 삭제
    myBookReviewRepository.deleteAllByUserId(userId);
    log.debug("[회원탈퇴] 도서 리뷰 삭제 완료: userId={}", userId);

    // 도서 카운트 삭제
    myBookCountRepository.findByUserId(userId).ifPresent(count -> {
      myBookCountRepository.delete(count);
      log.debug("[회원탈퇴] 도서 카운트 삭제 완료: countId={}", count.getId());
    });

    // 도서 데이터 삭제
    List<MyBook> myBooks = myBookRepository.findAllByUserId(userId);
    if (!myBooks.isEmpty()) {
      myBookRepository.deleteAll(myBooks);
      log.debug("[회원탈퇴] 도서 데이터 삭제 완료: {}개", myBooks.size());
    }
  }

  private void deleteCdRelatedData(Long userId) {
    // CD 댓글 삭제
    List<CdComment> comments = cdCommentRepository.findAllByUserId(userId);
    if (!comments.isEmpty()) {
      cdCommentRepository.deleteAll(comments);
      log.debug("[회원탈퇴] CD 댓글 삭제 완료: {}개", comments.size());
    }

    // CD 데이터 삭제
    List<MyCd> myCds = myCdRepository.findByUserId(userId);
    if (!myCds.isEmpty()) {
      myCdRepository.deleteAll(myCds);
      log.debug("[회원탈퇴] CD 데이터 삭제 완료: {}개", myCds.size());
    }

    // CD 카운트 삭제
    roomRepository.findByUserId(userId).ifPresent(room -> {
      myCdCountRepository.findByRoom(room).ifPresent(count -> {
        myCdCountRepository.delete(count);
        log.debug("[회원탈퇴] CD 카운트 삭제 완료: countId={}", count.getId());
      });
    });
  }

  private void deletePointData(Long userId) {
    // 포인트 히스토리 삭제
    int deletedCount = pointHistoryRepository.deleteByUserId(userId);
    log.debug("[회원탈퇴] 포인트 내역 삭제 완료: {}개 삭제됨", deletedCount);

    // 포인트 삭제
    pointRepository.findByUserId(userId).ifPresent(point -> {
      pointRepository.delete(point);
      log.debug("[회원탈퇴] 포인트 삭제 완료: pointId={}", point.getId());
    });
  }
}