package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.room.dto.*;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MockRoomControllerTest {

    @InjectMocks
    private MockRoomController mockRoomController;

    private RoomResponseDto mockRoom;
    private UpdateRoomThemeResponseDto updateRoomThemeResponseDto;
    private ToggleFurnitureResponseDto toggleFurnitureResponseDto;

    @BeforeEach
    void setUp() {
        mockRoom = RoomResponseDto.builder()
                .roomId(1L)
                .userId(67890L)
                .theme("basic")
                .createdAt(LocalDateTime.now())
                .furnitures(List.of(
                        new FurnitureResponseDto("BOOKSHELF", true, 3, 100, List.of("Fantasy", "Mystery")),
                        new FurnitureResponseDto("CD_RACK", false, 1, 50, List.of("Rock", "Pop"))
                ))
                .storageLimits(new StorageLimitsDto(100, 50))
                .userStorage(new UserStorageDto(25L, 10L, 5L, 7L))
                .build();

        updateRoomThemeResponseDto = new UpdateRoomThemeResponseDto(1L, "forest");

        toggleFurnitureResponseDto = new ToggleFurnitureResponseDto(
                1L,
                new FurnitureResponseDto("BOOKSHELF", false, 3, 100, List.of("Fantasy", "Mystery"))
        );
    }

    @Test
    @DisplayName("방 ID로 Mock 방 정보를 정상적으로 조회할 수 있다")
    void testGetRoomById_Success() {
        ResponseEntity<RoomResponseDto> response = mockRoomController.getRoom(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getRoomId());
    }

    @Test
    @DisplayName("사용자 ID로 Mock 방 정보를 정상적으로 조회할 수 있다")
    void testGetRoomByUserId_Success() {
        ResponseEntity<RoomResponseDto> response = mockRoomController.getRoomByUserId(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getRoomId());
    }

    @Test
    @DisplayName("Mock 방 테마 변경이 정상적으로 수행된다")
    void testUpdateRoomTheme_Success() {
        UpdateRoomThemeRequestDto requestDto = new UpdateRoomThemeRequestDto("marine");

        ResponseEntity<UpdateRoomThemeResponseDto> response = mockRoomController.updateRoomTheme(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("marine", response.getBody().getUpdatedTheme());
    }

    @Test
    @DisplayName("Mock 책꽂이 토글이 정상적으로 수행된다")
    void testToggleBookshelfVisibility_Success() {
        FurnitureRequestDto requestDto = new FurnitureRequestDto("BOOKSHELF");

        ResponseEntity<ToggleFurnitureResponseDto> response = mockRoomController.toggleFurnitureVisibility(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().getFurniture().getIsVisible());
    }

    @Test
    @DisplayName("Mock CD랙 상태 토글이 정상적으로 수행된다")
    void testToggleCDRACKVisibility_Success() {
        FurnitureRequestDto requestDto = new FurnitureRequestDto("CD_RACK");

        ResponseEntity<ToggleFurnitureResponseDto> response = mockRoomController.toggleFurnitureVisibility(1L, 1L, requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        assertTrue(response.getBody().getFurniture().getIsVisible());
    }

    @Test
    @DisplayName("존재하지 않는 가구를 토글하면 예외가 발생한다")
    void testToggleFurnitureVisibility_InvalidFurniture() {
        FurnitureRequestDto requestDto = new FurnitureRequestDto("INVALID_TYPE");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mockRoomController.toggleFurnitureVisibility(1L, 1L, requestDto));

        assertEquals(ErrorCode.INVALID_FURNITURE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("가구 타입이 null이면 INVALID_FURNITURE_TYPE 예외 발생한다")
    void testToggleFurnitureVisibility_NullFurnitureType() {
        // Given
        FurnitureRequestDto requestDto = new FurnitureRequestDto(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                mockRoomController.toggleFurnitureVisibility(1L, 1L, requestDto));

        assertEquals(ErrorCode.INVALID_FURNITURE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 가구 타입을 전달하면 INVALID_FURNITURE_TYPE 예외 발생")
    void testToggleFurnitureVisibility_FurnitureNotFound() {
        // Given
        FurnitureRequestDto requestDto = new FurnitureRequestDto("NON_EXISTENT_FURNITURE"); // 존재하지 않는 가구 타입

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                mockRoomController.toggleFurnitureVisibility(1L, 1L, requestDto));

        assertEquals(ErrorCode.INVALID_FURNITURE_TYPE, exception.getErrorCode());
    }





    @Test
    @DisplayName("Mock - 사용자가 잠금 해제한 테마 목록 조회가 정상적으로 수행된다")
    void testGetUnlockedThemes_Success() {
        ResponseEntity<List<String>> response = mockRoomController.getUnlockedThemes(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().contains("basic"));
        assertTrue(response.getBody().contains("forest"));
        assertTrue(response.getBody().contains("marine"));
    }
}
