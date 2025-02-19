package com.roome.domain.room.service;

import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoomResponseDto createRoom(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Room newRoom = Room.builder()
                .user(user)
                .theme(RoomTheme.BASIC)
                .build();

        Room savedRoom = roomRepository.save(newRoom);

        int savedMusic = 0;
        int savedBooks = 0;
        int writtenReviews = 0;
        int writtenMusicLogs = 0;

        return RoomResponseDto.from(savedRoom, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        int savedMusic = fetchSavedMusicCount(room.getUser().getId());
        int savedBooks = fetchSavedBooksCount(room.getUser().getId());
        int writtenReviews = fetchWrittenReviewsCount(room.getUser().getId());
        int writtenMusicLogs = fetchWrittenMusicLogsCount(room.getUser().getId());

        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByUserId(Long userId){
        Room room = roomRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        int savedMusic = fetchSavedMusicCount(userId);
        int savedBooks = fetchSavedBooksCount(userId);
        int writtenReviews = fetchWrittenReviewsCount(userId);
        int writtenMusicLogs = fetchWrittenMusicLogsCount(userId);

        return RoomResponseDto.from(room, savedMusic, savedBooks, writtenReviews, writtenMusicLogs);

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

    private int fetchSavedMusicCount(Long userId) {
        return 0;
    }

    private int fetchSavedBooksCount(Long userId) {
        return 0;
    }

    private int fetchWrittenReviewsCount(Long userId) {
        return 0;
    }

    private int fetchWrittenMusicLogsCount(Long userId) {
        return 0;
    }
}
