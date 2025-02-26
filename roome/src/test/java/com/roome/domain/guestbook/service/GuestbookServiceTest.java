package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestbookServiceTest {

    @Mock
    private GuestbookRepository guestbookRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GuestbookService guestbookService;


    @Test
    @DisplayName("방명록을 조회할 수 있다")
    public void testGetGuestbook_Success() {
        Long roomId = 1L;
        Room room = Room.builder()
                .id(roomId)
                .build();

        User user = User.builder()
                .id(1L)
                .nickname("User")
                .profileImage("profile.jpg")
                .build();

        Guestbook guestbook = Guestbook.builder()
                .guestbookId(1L)
                .room(room)
                .user(user)
                .nickname("User")
                .profileImage("profile.jpg")
                .message("Great room!")
                .relation(RelationType.하우스메이트)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(java.util.Optional.of(room));
        when(guestbookRepository.findByRoom(eq(room), any())).thenReturn(new PageImpl<>(Collections.singletonList(guestbook), PageRequest.of(0, 10), 1));

        var result = guestbookService.getGuestbook(roomId, 1, 10);
        assertNotNull(result);
        assertEquals(roomId, result.getRoomId());
        assertEquals(1, result.getGuestbook().size());
        assertEquals("Great room!", result.getGuestbook().get(0).getMessage());
    }


    @Test
    @DisplayName("존재하지 않는 방에 대한 방명록 조회 시 예외가 발생한다")
    public void testGetGuestbook_RoomNotFound() {
        Long roomId = 999L;

        when(roomRepository.findById(roomId)).thenReturn(java.util.Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookService.getGuestbook(roomId, 1, 10));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("존재하지 않는 방에 방명록을 추가할 수 없다")
    public void testAddGuestbook_RoomNotFound() {
        Long roomId = 999L;
        Long userId = 1L;
        GuestbookRequestDto requestDto = GuestbookRequestDto.builder()
                .message("Nice place!")
                .build();

        when(roomRepository.findById(roomId)).thenReturn(java.util.Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookService.addGuestbook(roomId, userId, requestDto));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 사용자에 대해 방명록을 추가할 수 없다")
    public void testAddGuestbook_UserNotFound() {
        Long roomId = 1L;
        Long userId = 999L;
        Room room = Room.builder()
                .id(roomId)
                .build();

        GuestbookRequestDto requestDto = GuestbookRequestDto.builder()
                .message("Nice place!")
                .build();

        when(roomRepository.findById(roomId)).thenReturn(java.util.Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookService.addGuestbook(roomId, userId, requestDto));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방명록을 삭제할 수 있다")
    public void testDeleteGuestbook_Success() {
        Long guestbookId = 1L;
        Long userId = 1L;

        Guestbook guestbook = Guestbook.builder()
                .guestbookId(guestbookId)
                .user(User.builder().id(userId).build())
                .build();

        when(guestbookRepository.findById(guestbookId)).thenReturn(java.util.Optional.of(guestbook));
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(guestbook.getUser()));

        guestbookService.deleteGuestbook(guestbookId, userId);

        verify(guestbookRepository, times(1)).delete(guestbook);
    }

    @Test
    @DisplayName("존재하지 않는 방명록을 삭제할 수 없다")
    public void testDeleteGuestbook_GuestbookNotFound() {
        Long guestbookId = 999L;
        Long userId = 1L;

        when(guestbookRepository.findById(guestbookId)).thenReturn(java.util.Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookService.deleteGuestbook(guestbookId, userId));
        assertEquals(ErrorCode.GUESTBOOK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방 소유주나 방명록 작성자가 아니면 방명록을 삭제할 수 없다")
    public void testDeleteGuestbook_UserNotAuthorized() {
        Long guestbookId = 1L;
        Long userId = 999L;

        User guestbookUser = User.builder().id(1L).build(); // 다른 사용자
        Guestbook guestbook = Guestbook.builder()
                .guestbookId(guestbookId)
                .user(guestbookUser)
                .build();

        when(guestbookRepository.findById(guestbookId)).thenReturn(java.util.Optional.of(guestbook));

        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(mockUser));

        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookService.deleteGuestbook(guestbookId, userId));
        assertEquals(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN, exception.getErrorCode());
    }

}
