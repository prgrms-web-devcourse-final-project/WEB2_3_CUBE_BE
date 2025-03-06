package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.room.dto.*;
import com.roome.domain.room.service.RoomService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @InjectMocks
    private RoomController roomController;

    @Mock
    private RoomService roomService;

    private RoomResponseDto roomResponseDto;
    private UpdateRoomThemeResponseDto updateRoomThemeResponseDto;
    private ToggleFurnitureResponseDto toggleFurnitureResponseDto;

    @BeforeEach
    void setUp() {
        roomResponseDto = RoomResponseDto.builder()
                .roomId(1L)
                .userId(1L)
                .theme("basic")
                .nickname("testNickname")
                .createdAt(LocalDateTime.now())
                .furnitures(List.of(new FurnitureResponseDto("BOOKSHELF", true, 1, 20, List.of("Fantasy", "Horror"))))
                .storageLimits(new StorageLimitsDto(100, 50))
                .userStorage(new UserStorageDto(25L, 10L, 5L, 7L))
                .topBookGenres(List.of("Fantasy", "Sci-Fi"))
                .topCdGenres(List.of("Rock", "Pop"))
                .build();

        updateRoomThemeResponseDto = new UpdateRoomThemeResponseDto(1L, "vintage");
        toggleFurnitureResponseDto = new ToggleFurnitureResponseDto(1L,
                new FurnitureResponseDto("BOOKSHELF", true, 1, 20, List.of("Fantasy", "Mystery", "Horror"))
        );
    }


    @Test
    @DisplayName("방 ID로 방 정보를 조회하면 정상적으로 반환된다")
    void testGetRoomById_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponseDto);

        ResponseEntity<RoomResponseDto> response = roomController.getRoom(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getRoomId());

        verify(roomService, times(1)).getRoomById(1L);
    }

    @Test
    @DisplayName("사용자 ID로 방 정보를 조회하면 정상적으로 반환된다")
    void testGetRoomByUserId_Success() {
        when(roomService.getRoomByUserId(1L)).thenReturn(roomResponseDto);

        ResponseEntity<RoomResponseDto> response = roomController.getRoomByUserId(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getRoomId());

        verify(roomService, times(1)).getRoomByUserId(1L);
    }

    @Test
    @DisplayName("방 테마를 변경하면 정상적으로 반영된다")
    void testUpdateRoomTheme_Success() {
        UpdateRoomThemeRequestDto requestDto = new UpdateRoomThemeRequestDto("marine");
        when(roomService.updateRoomTheme(1L, 1L, "marine")).thenReturn("marine");

        ResponseEntity<UpdateRoomThemeResponseDto> response = roomController.updateRoomTheme(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("marine", response.getBody().getUpdatedTheme());

        verify(roomService, times(1)).updateRoomTheme(1L, 1L, "marine");
    }

    @Test
    @DisplayName("가구 표시 여부를 토글하면 정상적으로 반영된다")
    void testToggleFurnitureVisibility_Success() {
        FurnitureRequestDto requestDto = new FurnitureRequestDto("BOOKSHELF");
        when(roomService.toggleFurnitureVisibility(1L, 1L, "BOOKSHELF"))
                .thenReturn(new FurnitureResponseDto("BOOKSHELF", true, 1, 20, List.of("Fantasy", "Mystery", "Horror")));

        ResponseEntity<ToggleFurnitureResponseDto> response = roomController.toggleFurnitureVisibility(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getFurniture().getIsVisible());

        verify(roomService, times(1)).toggleFurnitureVisibility(1L, 1L, "BOOKSHELF");
    }

    @Test
    @DisplayName("사용자가 잠금 해제한 테마 목록을 조회하면 정상적으로 반환된다")
    void testGetUnlockedThemes_Success() {
        when(roomService.getUnlockedThemes(1L)).thenReturn(List.of("BASIC", "MARINE"));

        ResponseEntity<List<String>> response = roomController.getUnlockedThemes(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("BASIC"));
        assertTrue(response.getBody().contains("MARINE"));

        verify(roomService, times(1)).getUnlockedThemes(1L);
    }

    @Test
    @DisplayName("방을 방문하면 정상적으로 조회된다")
    void testVisitRoom_Success() {
        when(roomService.visitRoomByRoomId(2L, 1L)).thenReturn(roomResponseDto);

        ResponseEntity<RoomResponseDto> response = roomController.visitRoom(2L, 1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getRoomId());

        verify(roomService, times(1)).visitRoomByRoomId(2L, 1L);
    }

    @Test
    @DisplayName("사용자 ID로 방을 방문하면 정상적으로 조회된다")
    void testVisitUserRoom_Success() {
        Long visitorId = 2L;
        Long hostId = 1L;

        when(roomService.visitRoomByHostId(visitorId, hostId)).thenReturn(roomResponseDto);

        ResponseEntity<RoomResponseDto> response = roomController.visitUserRoom(visitorId, hostId);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getRoomId());

        verify(roomService, times(1)).visitRoomByHostId(visitorId, hostId);
    }


    @Test
    @DisplayName("존재하지 않는 방을 조회하면 예외가 발생한다")
    void testGetRoomById_NotFound() {
        when(roomService.getRoomById(999L)).thenThrow(new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        BusinessException exception = assertThrows(BusinessException.class, () -> roomController.getRoom(999L));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomService, times(1)).getRoomById(999L);
    }

    @Test
    @DisplayName("잘못된 방 테마를 설정하면 예외가 발생한다")
    void testUpdateRoomTheme_InvalidTheme() {
        UpdateRoomThemeRequestDto requestDto = new UpdateRoomThemeRequestDto("INVALID_THEME");

        when(roomService.updateRoomTheme(1L, 1L, "INVALID_THEME"))
                .thenThrow(new BusinessException(ErrorCode.INVALID_ROOM_THEME));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> roomController.updateRoomTheme(1L, 1L, requestDto));

        assertEquals(ErrorCode.INVALID_ROOM_THEME, exception.getErrorCode());
        verify(roomService, times(1)).updateRoomTheme(1L, 1L, "INVALID_THEME");
    }
}
