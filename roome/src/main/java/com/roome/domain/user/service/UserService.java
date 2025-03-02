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
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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

  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    try {
      // 1. 하우스메이트 관계 삭제
      deleteHousemateRelations(userId);

      // 2. 도서 관련 데이터 삭제
      deleteBookRelatedData(userId);

      // 3. CD 관련 데이터 삭제
      deleteCdRelatedData(userId);

      // 4. 방명록 삭제
      deleteGuestbookData(userId);

      // 5. 사용자 방 및 가구 삭제
      deleteRoomAndFurniture(userId);

      // 6. 포인트 및 포인트 내역 삭제
      deletePointData(userId);

      // 7. 사용자 삭제
      userRepository.delete(user);
      log.info("[회원탈퇴] 사용자 삭제 완료: {}", userId);

    } catch (Exception e) {
      String errorMsg = String.format("[회원탈퇴] 연관 데이터 삭제 실패 - 롤백 수행: userId=%s, 오류=%s", userId,
          e.getMessage());
      log.error(errorMsg, e);
      throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다.");
    }
  }

  private void deleteHousemateRelations(Long userId) {
    try {
      // 양방향 관계 모두 삭제 (내가 추가한 것 + 나를 추가한 것)
      int count = housemateRepository.deleteByUserIdOrAddedId(userId, userId);
      log.debug("[회원탈퇴] 하우스메이트 관계 삭제 수: {}", count);
    } catch (Exception e) {
      log.error("[회원탈퇴] 하우스메이트 관계 삭제 실패: userId={}, 오류={}", userId, e.getMessage());
      throw new RuntimeException("하우스메이트 관계 삭제 중 오류 발생: " + e.getMessage(), e);
    }
  }

  private void deleteRoomAndFurniture(Long userId) {
    roomRepository.findByUserId(userId).ifPresentOrElse(room -> {
      try {
        List<Furniture> furnitures = furnitureRepository.findByRoomId(room.getId());

        // 방 가구 삭제
        if (!furnitures.isEmpty()) {
          furnitureRepository.deleteAll(furnitures);
          log.debug("[회원탈퇴] 가구 삭제 완료: {}개", furnitures.size());
        }

        // 방을 다시 조회하여 최신 상태 유지
        room = roomRepository.findById(room.getId()).orElse(null);
        if (room == null) {
          log.warn("[회원탈퇴] 이미 삭제된 방 (roomId={})", room.getId());
          return;
        }

        // 방 삭제
        roomRepository.delete(room);
        log.debug("[회원탈퇴] 방 삭제 완료: roomId={}", room.getId());
      } catch (Exception e) {
        log.error("[회원탈퇴] 방 및 가구 삭제 실패: roomId={}, 오류={}", room.getId(), e.getMessage());
        throw new RuntimeException("방 및 가구 삭제 중 오류 발생: " + e.getMessage(), e);
      }
    }, () -> log.info("[회원탈퇴] 방이 존재하지 않음: userId={}", userId));
  }

  private void deleteBookRelatedData(Long userId) {
    try {
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
    } catch (Exception e) {
      log.error("[회원탈퇴] 도서 관련 데이터 삭제 실패: userId={}, 오류={}", userId, e.getMessage());
      throw new RuntimeException("도서 관련 데이터 삭제 중 오류 발생: " + e.getMessage(), e);
    }
  }

  private void deleteCdRelatedData(Long userId) {
    try {
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
    } catch (Exception e) {
      log.error("[회원탈퇴] CD 관련 데이터 삭제 실패: userId={}, 오류={}", userId, e.getMessage());
      throw new RuntimeException("CD 관련 데이터 삭제 중 오류 발생: " + e.getMessage(), e);
    }
  }

  private void deleteGuestbookData(Long userId) {
    try {
      // 내 방의 방명록과 내가 작성한 방명록 모두 삭제
      roomRepository.findByUserId(userId).ifPresentOrElse(room -> {
        List<Guestbook> guestbooks = guestbookRepository.findAllByRoomOrUserId(room, userId);
        if (!guestbooks.isEmpty()) {
          guestbookRepository.deleteAll(guestbooks);
          log.debug("[회원탈퇴] 방명록 삭제 완료: {}개", guestbooks.size());
        }
      }, () -> {
        // 방이 없는 경우 사용자가 작성한 방명록만 삭제
        List<Guestbook> guestbooks = guestbookRepository.findAllByUserId(userId);
        if (!guestbooks.isEmpty()) {
          guestbookRepository.deleteAll(guestbooks);
          log.debug("[회원탈퇴] 사용자 작성 방명록만 삭제 완료: {}개", guestbooks.size());
        }
      });
    } catch (Exception e) {
      log.error("[회원탈퇴] 방명록 데이터 삭제 실패: userId={}, 오류={}", userId, e.getMessage());
      throw new RuntimeException("방명록 데이터 삭제 중 오류 발생: " + e.getMessage(), e);
    }
  }

  private void deletePointData(Long userId) {
    try {
      // 포인트 히스토리 삭제
      int deletedCount = pointHistoryRepository.deleteByUserId(userId);
      log.debug("[회원탈퇴] 포인트 내역 삭제 완료: {}개 삭제됨", deletedCount);

      // 포인트 삭제
      pointRepository.findByUserId(userId).ifPresent(point -> {
        pointRepository.delete(point);
        log.debug("[회원탈퇴] 포인트 삭제 완료: pointId={}", point.getId());
      });

    } catch (Exception e) {
      log.error("[회원탈퇴] 포인트 데이터 삭제 실패: userId={}, 오류={}", userId, e.getMessage());
      throw new RuntimeException("포인트 데이터 삭제 중 오류 발생: " + e.getMessage(), e);
    }
  }
}