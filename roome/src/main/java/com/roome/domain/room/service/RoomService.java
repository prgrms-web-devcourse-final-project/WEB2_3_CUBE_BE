package com.roome.domain.room.service;

import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final FurnitureRepository furnitureRepository;
    private final MyCdCountRepository myCdCountRepository;
    private final MyBookCountRepository myBookCountRepository;
    private final MyBookReviewRepository myBookReviewRepository;
    private final CdCommentRepository cdCommentRepository;

    @Transactional
    public RoomResponseDto createRoom(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("방 생성 실패: 사용자(userId={})를 찾을 수 없음", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        Room newRoom = Room.builder()
                .user(user)
                .theme(RoomTheme.BASIC)
                .furnitures(new ArrayList<>()) // 빈 리스트 초기화
                .build();

        Room savedRoom = roomRepository.save(newRoom);
        roomRepository.flush();

        // 기본 가구 추가 및 저장
        List<Furniture> defaultFurnitures = addDefaultFurniture(savedRoom);

        // 방에 가구 추가
        savedRoom.getFurnitures().addAll(defaultFurnitures);

        // 가구 생성 완료 로그
        defaultFurnitures.forEach(furniture ->
                log.info("가구 생성 완료: 방(roomId={})에 가구({}) 추가됨", savedRoom.getId(), furniture.getFurnitureType())
        );

        Long savedMusic = 0L;
        Long savedBooks = 0L;
        Long writtenReviews = 0L;
        Long writtenMusicLogs = 0L;

        log.info("방 생성 완료: 방(roomId={}) 생성됨 (userId={})", savedRoom.getId(), userId);
        return RoomResponseDto.from(savedRoom, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    private List<Furniture> addDefaultFurniture(Room room) {
        try {
            List<Furniture> defaultFurniture = new ArrayList<>();
            defaultFurniture.add(new Furniture(room, FurnitureType.BOOKSHELF));
            defaultFurniture.add(new Furniture(room, FurnitureType.CD_RACK));

            List<Furniture> savedFurniture = furnitureRepository.saveAll(defaultFurniture);

            log.info("기본 가구 생성 완료: roomId={}, 가구 개수={}", room.getId(), savedFurniture.size());
            return savedFurniture;
        } catch (Exception e) {
            log.error("기본 가구 추가 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("기본 가구 추가 실패", e);
        }
    }




    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("방 조회 실패: 존재하지 않는 방 (roomId={})", roomId);
                    return new BusinessException(ErrorCode.ROOM_NOT_FOUND);
                });

        Long savedMusic = fetchSavedMusicCount(room);
        Long savedBooks = fetchSavedBooksCount(roomId);
        Long writtenReviews = fetchWrittenReviewsCount(room.getUser().getId());
        Long writtenMusicLogs = fetchWrittenMusicLogsCount(room.getUser().getId());

        log.info("방 조회 성공: 방(roomId={}) 조회 완료", roomId);
        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByUserId(Long userId) {
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("방 조회 실패: 해당 사용자의 방이 존재하지 않음 (userId={})", userId);
                    return new BusinessException(ErrorCode.ROOM_NOT_FOUND);
                });

        log.info("방 조회 성공: 사용자의 방(roomId={})을 조회 완료 (userId={})", room.getId(), userId);
        return buildRoomResponse(room);
    }

    // 에러 처리 중
    @Transactional
    public RoomResponseDto getOrCreateRoomByUserId(Long userId) {
        return roomRepository.findByUserId(userId)
                .map(this::buildRoomResponse)
                .orElseGet(() -> {
                    try {
                        return createRoom(userId);
                    } catch (BusinessException e) {
                        log.error("방을 생성하려 했으나 사용자(userId={})를 찾을 수 없음", userId, e);
                        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                    }
                });
    }

    @Transactional
    public String updateRoomTheme(Long userId, Long roomId, String newTheme){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("방 테마 변경 실패: 존재하지 않는 방 (roomId={})", roomId);
                    return new BusinessException(ErrorCode.ROOM_NOT_FOUND);
                });

        if (!room.getUser().getId().equals(userId)) {
            log.error("방 테마 변경 실패: 사용자(userId={})가 방(roomId={})의 소유자가 아님", userId, roomId);
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        RoomTheme theme = RoomTheme.fromString(newTheme);
        room.updateTheme(theme);
        log.info("방 테마 변경 완료: 방(roomId={}) → 새 테마({})", roomId, newTheme);

        return theme.name();
    }

    @Transactional
    public FurnitureResponseDto toggleFurnitureVisibility(Long userId, Long roomId, String furnitureTypeStr) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("가구 상태 변경 실패: 존재하지 않는 방 (roomId={})", roomId);
                    return new BusinessException(ErrorCode.ROOM_NOT_FOUND);
                });

        if (!room.getUser().getId().equals(userId)) {
            log.error("가구 상태 변경 실패: 사용자(userId={})가 방(roomId={})의 소유자가 아님", userId, roomId);
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        FurnitureType furnitureType;
        try {
            furnitureType = FurnitureType.valueOf(furnitureTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("가구 상태 변경 실패: 유효하지 않은 가구 타입 (입력값={})", furnitureTypeStr);
            throw new BusinessException(ErrorCode.INVALID_FURNITURE_TYPE);
        }

        Furniture furniture = room.getFurnitures().stream()
                .filter(f -> f.getFurnitureType() == furnitureType)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("가구 상태 변경 실패: 방(roomId={})에 해당 가구({})가 존재하지 않음", roomId, furnitureTypeStr);
                    return new BusinessException(ErrorCode.FURNITURE_NOT_FOUND);
                });

        // 가구 활성화/비활성화 토글
        boolean newVisibility = !furniture.getIsVisible();
        furniture.setVisible(newVisibility);

        log.info("가구 상태 변경 완료: 방(roomId={}), 가구({}), 새 상태={}", roomId, furnitureType, newVisibility);
        return FurnitureResponseDto.from(furniture);
    }



    private RoomResponseDto buildRoomResponse(Room room) {
        Long savedMusic = fetchSavedMusicCount(room);
        Long savedBooks = fetchSavedBooksCount(room.getId());
        Long writtenReviews = fetchWrittenReviewsCount(room.getUser().getId());
        Long writtenMusicLogs = fetchWrittenMusicLogsCount(room.getUser().getId());

        log.info("방 정보 생성 완료: 방(roomId={}), 저장된 음악={}, 저장된 책={}, 작성한 리뷰={}, 작성한 음악 로그={}",
                room.getId(), savedMusic, savedBooks, writtenReviews, writtenMusicLogs);

        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }


    Long fetchSavedMusicCount(Room room) {
        try {
            return myCdCountRepository.findByRoom(room)
                    .map(MyCdCount::getCount)
                    .orElse(0L);
        } catch (Exception e) {
            log.error("음악 저장 개수 조회 중 오류 발생 (roomId={})", room.getId(), e);
            return 0L;
        }
    }

    Long fetchSavedBooksCount(Long roomId) {
        try {
            return myBookCountRepository.findByRoomId(roomId)
                    .map(MyBookCount::getCount)
                    .orElse(0L);
        } catch (Exception e) {
            log.error("책 저장 개수 조회 중 오류 발생 (roomId={})", roomId, e);
            return 0L;
        }
    }

    Long fetchWrittenReviewsCount(Long userId) {
        try {
            return myBookReviewRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("사용자의 작성한 리뷰 개수 조회 중 오류 발생 (userId={})", userId, e);
            return 0L;
        }
    }

    Long fetchWrittenMusicLogsCount(Long userId) {
        try {
            return cdCommentRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("사용자의 작성한 음악 로그 개수 조회 중 오류 발생 (userId={})", userId, e);
            return 0L;
        }
    }

}