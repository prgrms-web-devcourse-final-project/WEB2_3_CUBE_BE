package com.roome.domain.room.service;

import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.domain.room.exception.RoomNoFoundException;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MyCdCountRepository myCdCountRepository;
    @Mock
    private MyBookCountRepository myBookCountRepository;
    @Mock
    private MyBookReviewRepository myBookReviewRepository;
    @Mock
    private CdCommentRepository cdCommentRepository;
    @Mock
    private FurnitureRepository furnitureRepository;

    private User user;
    private Room room;
    private Furniture bookshelf;
    private Furniture cdRack;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        user = User.builder().id(1L).build();

        // 테스트용 방 생성
        room = Room.builder()
                .id(1L)
                .user(user)
                .theme(RoomTheme.BASIC)
                .furnitures(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // 기본 가구 (책꽂이 & CD 랙)
        bookshelf = Furniture.builder()
                .id(1L)
                .room(room)
                .furnitureType(FurnitureType.BOOKSHELF)
                .isVisible(false)
                .level(1)
                .build();

        cdRack = Furniture.builder()
                .id(2L)
                .room(room)
                .furnitureType(FurnitureType.CD_RACK)
                .isVisible(false)
                .level(1)
                .build();

        // 방에 기본 가구 추가
        room.getFurnitures().add(bookshelf);
        room.getFurnitures().add(cdRack);
    }

    @Test
    @DisplayName("방을 생성하면 기본 가구가 추가된다")
    void testCreateRoom_Success() {
        // give
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // when
        RoomResponseDto response = roomService.createRoom(user.getId());

        // then
        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals("basic", response.getTheme());
        assertEquals(2, response.getFurnitures().size());

        verify(roomRepository).save(any(Room.class));
        verify(furnitureRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 방을 생성하려 하면 예외가 발생한다")
    void testCreateRoom_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.createRoom(999L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자의 방이 이미 존재하면 해당 방을 반환한다")
    void testGetOrCreateRoomByUserId_ExistingRoom() {
        // Given
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDto response = roomService.getOrCreateRoomByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(room.getId(), response.getRoomId());
        assertEquals(userId, response.getUserId());

        verify(roomRepository, times(1)).findByUserId(userId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자의 방이 존재하지 않으면 새 방을 생성하고 반환한다")
    void testGetOrCreateRoomByUserId_CreateNewRoom() {
        // Given
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user)); // 새 방을 생성할 사용자 존재

        Room newRoom = Room.builder().id(2L).user(user).theme(RoomTheme.BASIC).build();
        when(roomRepository.save(any(Room.class))).thenReturn(newRoom);

        // When
        RoomResponseDto response = roomService.getOrCreateRoomByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(2L, response.getRoomId());
        assertEquals(userId, response.getUserId());

        verify(roomRepository, times(1)).findByUserId(userId);
        verify(roomRepository, times(1)).save(any(Room.class)); // 새로운 방이 저장되어야 함
    }

    @Test
    @DisplayName("방이 존재하지 않고 방 생성 시 사용자도 존재하지 않으면 예외가 발생한다")
    void testGetOrCreateRoomByUserId_UserNotFoundInCreateRoom() {
        // Given
        Long userId = 1L;

        when(roomRepository.findByUserId(userId)).thenReturn(Optional.empty());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.getOrCreateRoomByUserId(userId));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        verify(roomRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).findById(userId); // 사용자 조회가 한 번 실행됨
        verify(roomRepository, never()).save(any()); // 방이 생성되지 않아야 함
    }









    @Test
    @DisplayName("방 ID로 방을 정상적으로 조회할 수 있다")
    void testGetRoomById_Success() {
        // Given
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // 저장된 음악 개수 설정
        MyCdCount myCdCount = MyCdCount.builder()
                .room(room)
                .count(5L)
                .build();
        when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));

        // 저장된 책 개수 설정
        MyBookCount myBookCount = MyBookCount.builder()
                .room(room)
                .user(user)
                .count(3L)
                .build();
        when(myBookCountRepository.findByRoomId(roomId)).thenReturn(Optional.of(myBookCount));

        // 작성된 리뷰 및 음악 로그 개수 설정
        when(myBookReviewRepository.countByUserId(user.getId())).thenReturn(2L);
        when(cdCommentRepository.countByUserId(user.getId())).thenReturn(4L);

        // When
        RoomResponseDto response = roomService.getRoomById(roomId);

        // Then
        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(room.getTheme().getThemeName(), response.getTheme());

        // 저장 데이터 검증
        assertEquals(5L, response.getUserStorage().getSavedMusic());
        assertEquals(3L, response.getUserStorage().getSavedBooks());
        assertEquals(2L, response.getUserStorage().getWrittenReviews());
        assertEquals(4L, response.getUserStorage().getWrittenMusicLogs());

        verify(roomRepository, times(1)).findById(roomId);
    }


    @Test
    @DisplayName("존재하지 않는 방을 조회하면 예외가 발생한다")
    void testGetRoomById_RoomNotFound() {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.getRoomById(999L));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }






    @Test
    @DisplayName("사용자 ID로 방을 정상적으로 조회할 수 있다")
    void testGetRoomByUserId_Success() {
        // Given
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));

        // 저장된 음악 개수 설정
        MyCdCount myCdCount = MyCdCount.builder()
                .room(room)
                .count(7L)
                .build();
        when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));

        // 저장된 책 개수 설정
        MyBookCount myBookCount = MyBookCount.builder()
                .room(room)
                .user(user)
                .count(2L)
                .build();
        when(myBookCountRepository.findByRoomId(room.getId())).thenReturn(Optional.of(myBookCount));

        // 작성된 리뷰 및 음악 로그 개수 설정
        when(myBookReviewRepository.countByUserId(userId)).thenReturn(1L);
        when(cdCommentRepository.countByUserId(userId)).thenReturn(3L);

        // When
        RoomResponseDto response = roomService.getRoomByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(room.getId(), response.getRoomId());
        assertEquals(userId, response.getUserId());

        // 저장 데이터 검증
        assertEquals(7L, response.getUserStorage().getSavedMusic());
        assertEquals(2L, response.getUserStorage().getSavedBooks());
        assertEquals(1L, response.getUserStorage().getWrittenReviews());
        assertEquals(3L, response.getUserStorage().getWrittenMusicLogs());

        verify(roomRepository, times(1)).findByUserId(userId);
    }


    @Test
    @DisplayName("사용자의 방이 존재하지 않으면 예외가 발생한다")
    void testGetRoomByUserId_RoomNotFound() {
        // Given
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.getRoomByUserId(userId));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());

        verify(roomRepository, times(1)).findByUserId(userId);
    }






    @Test
    @DisplayName("방 테마를 정상적으로 변경할 수 있다")
    void testUpdateRoomTheme_Success() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        String newTheme = roomService.updateRoomTheme(user.getId(), room.getId(), "marine");

        assertEquals("MARINE", newTheme);
        assertEquals(RoomTheme.MARINE, room.getTheme());
    }

    @Test
    @DisplayName("방 테마 변경 시 잘못된 사용자면 예외가 발생한다")
    void testUpdateRoomTheme_AccessDenied() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.updateRoomTheme(999L, room.getId(), "forest"));
        assertEquals(ErrorCode.ROOM_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("방이 존재하지 않으면 테마 변경 시 예외가 발생한다")
    void testUpdateRoomTheme_RoomNotFound() {
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        String newTheme = "FOREST";

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.updateRoomTheme(userId, roomId, newTheme));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());

        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    @DisplayName("존재하지 않는 테마를 설정하면 예외가 발생한다")
    void testUpdateTheme_InvalidTheme(){
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        String invalidTheme = "INVALID_THEME";

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.updateRoomTheme(userId, roomId, invalidTheme));
        assertEquals(ErrorCode.INVALID_ROOM_THEME, exception.getErrorCode());

        verify(roomRepository, times(1)).findById(roomId);
    }






    @Test
    @DisplayName("가구를 토글할 수 있다")
    void testToggleFurniture_Success() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        FurnitureResponseDto updatedFurniture = roomService.toggleFurnitureVisibility(user.getId(), room.getId(), "BOOKSHELF");

        assertNotNull(updatedFurniture);
        assertEquals("BOOKSHELF", updatedFurniture.getFurnitureType());
        assertTrue(updatedFurniture.getIsVisible());
    }

    @Test
    @DisplayName("다른 사용자가 가구를 토글하면 예외가 발생한다")
    void testToggleFurniture_AccessDenied() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.toggleFurnitureVisibility(999L, room.getId(), "BOOKSHELF"));
        assertEquals(ErrorCode.ROOM_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 방의 가구를 토글하면 예외가 발생한다")
    void testToggleFurniture_RoomNotFound() {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.toggleFurnitureVisibility(1L, 999L, "BOOKSHELF"));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 가구를 토글하면 예외가 발생한다")
    void testToggleFurniture_FurnitureNotFound() {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(BusinessException.class, () -> roomService.toggleFurnitureVisibility(user.getId(), room.getId(), "NON_EXISTENT_FURNITURE"));
        assertEquals(ErrorCode.INVALID_FURNITURE_TYPE, exception.getErrorCode());
    }
}