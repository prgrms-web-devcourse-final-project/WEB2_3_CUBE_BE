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
        return RoomResponseDto.from(savedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return RoomResponseDto.from(room);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomByUserId(Long userId){
        return roomRepository.findByUserId(userId)
                .map(RoomResponseDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    @Transactional
    public RoomResponseDto updateRoomTheme(Long userId, Long roomId, String newTheme){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if(!room.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        RoomTheme theme = RoomTheme.fromString(newTheme);
        room.updateTheme(theme);

        return RoomResponseDto.from(room);
    }
}
