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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public RoomResponseDto createRoom(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Room newRoom = Room.builder()
                .user(user)
                .theme(RoomTheme.BASIC)
                .furnitures(new ArrayList<>())
                .build();

        Room savedRoom = roomRepository.save(newRoom);

        // 기본 가구 추가 (책꽂이 & CD 랙)
        List<Furniture> defaultFurnitures = List.of(
                Furniture.builder()
                        .room(savedRoom)
                        .furnitureType(FurnitureType.BOOKSHELF)
                        .isVisible(false)  // 기본값: 보이지 않음
                        .level(1)  // 기본값: 1레벨
                        .build(),
                Furniture.builder()
                        .room(savedRoom)
                        .furnitureType(FurnitureType.CD_RACK)
                        .isVisible(false)
                        .level(1)
                        .build()
        );

        furnitureRepository.saveAll(defaultFurnitures);

        Long savedMusic = 0L;
        Long savedBooks = 0L;
        Long writtenReviews = 0L;
        Long writtenMusicLogs = 0L;

        return RoomResponseDto.from(savedRoom, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Long savedMusic = fetchSavedMusicCount(room);
        Long savedBooks = fetchSavedBooksCount(roomId);
        Long writtenReviews = fetchWrittenReviewsCount(room.getUser().getId());
        Long writtenMusicLogs = fetchWrittenMusicLogsCount(room.getUser().getId());

        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByUserId(Long userId){
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return buildRoomResponse(room);
    }

    private RoomResponseDto buildRoomResponse(Room room) {
        Long savedMusic = fetchSavedMusicCount(room);
        Long savedBooks = fetchSavedBooksCount(room.getId());
        Long writtenReviews = fetchWrittenReviewsCount(room.getUser().getId());
        Long writtenMusicLogs = fetchWrittenMusicLogsCount(room.getUser().getId());

        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    // 에러 처리 중
    @Transactional
    public RoomResponseDto getOrCreateRoomByUserId(Long userId) {
        return roomRepository.findByUserId(userId)
                .map(this::buildRoomResponse)
                .orElseGet(() -> createRoom(userId));
    }

    @Transactional
    public String updateRoomTheme(Long userId, Long roomId, String newTheme){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if(!room.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        RoomTheme theme = RoomTheme.fromString(newTheme);
        room.updateTheme(theme);

        return theme.name();
    }

    @Transactional
    public FurnitureResponseDto toggleFurnitureVisibility(Long userId, Long roomId, String furnitureTypeStr) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        FurnitureType furnitureType;
        try {
            furnitureType = FurnitureType.valueOf(furnitureTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_FURNITURE_TYPE);
        }

        Furniture furniture = room.getFurnitures().stream()
                .filter(f -> f.getFurnitureType() == furnitureType)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FURNITURE_NOT_FOUND));

        furniture.setVisible(!furniture.getIsVisible()); // 활성화/비활성화 토글

        return FurnitureResponseDto.from(furniture);
    }

    private Long fetchSavedMusicCount(Room room) {
        return myCdCountRepository.findByRoom(room)
                .map(MyCdCount::getCount)
                .orElse(0L);
    }

    private Long fetchSavedBooksCount(Long roomId) {
        return myBookCountRepository.findByRoomId(roomId)
                .map(MyBookCount::getCount)
                .orElse(0L);
    }

    private Long fetchWrittenReviewsCount(Long userId) {
        return myBookReviewRepository.countByUserId(userId);
    }

    private Long fetchWrittenMusicLogsCount(Long userId) {
        return cdCommentRepository.countByUserId(userId);
    }
}