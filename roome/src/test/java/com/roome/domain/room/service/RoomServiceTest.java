package com.roome.domain.room.service;

import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

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

    @InjectMocks
    private RoomService roomService;

    private User user;
    private Room room;

    @BeforeEach
    void setUp() {
        // 테스트용 User, Room 객체 생성 (빌더에 id를 직접 넣거나 setter를 이용)
        user = User.builder().id(1L).build();
        room = Room.builder()
                .id(1L)  // 테스트용으로 id 설정 (실제 엔티티에서는 DB가 생성할 수 있음)
                .user(user)
                .theme(RoomTheme.BASIC)
                .furnitures(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateRoom_Success() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // roomRepository.save()가 호출될 때, id가 할당된 Room을 반환하도록 설정
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room saved = invocation.getArgument(0);
            // 테스트용으로 id를 강제로 할당
            setField(saved, "id", 1L);
            return saved;
        });

        RoomResponseDto response = roomService.createRoom(userId);

        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals(userId, response.getUserId());
        assertEquals("BASIC", response.getTheme());
        verify(userRepository).findById(userId);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testCreateRoom_UserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.createRoom(userId);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetRoomById_Success() {
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // myCdCountRepository stub
        MyCdCount myCdCount = mock(MyCdCount.class);
        when(myCdCount.getCount()).thenReturn(5L);
        when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));

        // myBookCountRepository stub
        MyBookCount myBookCount = mock(MyBookCount.class);
        when(myBookCount.getCount()).thenReturn(3L);
        when(myBookCountRepository.findByRoomId(roomId)).thenReturn(Optional.of(myBookCount));

        // 나머지 카운트 stub
        when(myBookReviewRepository.countByUserId(user.getId())).thenReturn(2L);
        when(cdCommentRepository.countByUserId(user.getId())).thenReturn(4L);

        RoomResponseDto response = roomService.getRoomById(roomId);

        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertEquals(user.getId(), response.getUserId());
        // RoomResponseDto 내부에서 storageLimits, userStorage를 생성하므로
        // 필요한 경우 해당 값들도 검증할 수 있음.
        verify(roomRepository).findById(roomId);
    }

    @Test
    void testGetRoomById_RoomNotFound() {
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.getRoomById(roomId);
        });
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetRoomByUserId_Success() {
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));

        MyCdCount myCdCount = mock(MyCdCount.class);
        when(myCdCount.getCount()).thenReturn(7L);
        when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));

        MyBookCount myBookCount = mock(MyBookCount.class);
        when(myBookCount.getCount()).thenReturn(2L);
        when(myBookCountRepository.findByRoomId(room.getId())).thenReturn(Optional.of(myBookCount));

        when(myBookReviewRepository.countByUserId(userId)).thenReturn(1L);
        when(cdCommentRepository.countByUserId(userId)).thenReturn(3L);

        RoomResponseDto response = roomService.getRoomByUserId(userId);

        assertNotNull(response);
        assertEquals(room.getId(), response.getRoomId());
        assertEquals(userId, response.getUserId());
        verify(roomRepository).findByUserId(userId);
    }

    @Test
    void testGetRoomByUserId_RoomNotFound() {
        Long userId = 1L;
        when(roomRepository.findByUserId(userId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.getRoomByUserId(userId);
        });
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateRoomTheme_Success() {
        Long userId = 1L;
        Long roomId = 1L;
        String newTheme = "MODERN";  // RoomTheme.fromString(newTheme)에서 MODERN이 올바르게 매핑되어야 함

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        // room.getUser().getId() == 1L (userId)

        String result = roomService.updateRoomTheme(userId, roomId, newTheme);
        assertEquals(newTheme, result);
        // room의 theme가 업데이트되었는지 확인 (RoomTheme의 getThemeName() 사용)
        assertEquals(newTheme, room.getTheme().getThemeName());
    }

    @Test
    void testUpdateRoomTheme_AccessDenied() {
        Long userId = 2L; // room의 userId는 1L이므로 접근 불가
        Long roomId = 1L;
        String newTheme = "MODERN";

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.updateRoomTheme(userId, roomId, newTheme);
        });
        assertEquals(ErrorCode.ROOM_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void testUpdateRoomTheme_RoomNotFound() {
        Long userId = 1L;
        Long roomId = 1L;
        String newTheme = "MODERN";

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.updateRoomTheme(userId, roomId, newTheme);
        });
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }
}