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
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

        // 6. 사용자 삭제
        userRepository.delete(user);

        log.info("User and related data successfully deleted for userId: {}", userId);
    }

    private void deleteHousemateRelations(Long userId) {
        // 양방향 관계 모두 삭제 (내가 추가한 것 + 나를 추가한 것)
        housemateRepository.deleteByUserIdOrAddedId(userId, userId);
    }

    private void deleteRoomAndFurniture(Long userId) {
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 방 가구 삭제
        List<Furniture> furnitures = furnitureRepository.findByRoomId(room.getId());
        furnitureRepository.deleteAll(furnitures);

        // 방 삭제
        roomRepository.delete(room);
    }

    private void deleteBookRelatedData(Long userId) {
        // 도서 리뷰 삭제
        myBookReviewRepository.deleteAllByUserId(userId);

        // 도서 카운트 삭제
        myBookCountRepository.findByUserId(userId)
                .ifPresent(myBookCountRepository::delete);

        // 도서 데이터 삭제
        List<MyBook> myBooks = myBookRepository.findAllByUserId(userId);
        myBookRepository.deleteAll(myBooks);
    }

    private void deleteCdRelatedData(Long userId) {
        // CD 댓글 삭제
        List<CdComment> comments = cdCommentRepository.findAllByUserId(userId);
        cdCommentRepository.deleteAll(comments);

        // CD 카운트 삭제
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        myCdCountRepository.findByRoom(room)
                .ifPresent(myCdCountRepository::delete);

        // CD 데이터 삭제
        List<MyCd> myCds = myCdRepository.findByUserId(userId);
        myCdRepository.deleteAll(myCds);
    }

    private void deleteGuestbookData(Long userId) {
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 내 방의 방명록과 내가 작성한 방명록 모두 삭제
        List<Guestbook> guestbooks = guestbookRepository.findAllByRoomOrUserId(room, userId);
        guestbookRepository.deleteAll(guestbooks);
    }

}