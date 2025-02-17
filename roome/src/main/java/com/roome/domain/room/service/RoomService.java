package com.roome.domain.room.service;

import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.exception.ErrorResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomResponseDto createRoom(Long userId){
        Room newRoom = Room.builder()
                .userId(userId)
                .theme("basic")
                .build();

        Room savedRoom = roomRepository.save(newRoom);
        return RoomResponseDto.from(savedRoom);
    }

    public RoomResponseDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return RoomResponseDto.from(room);
    }
}
