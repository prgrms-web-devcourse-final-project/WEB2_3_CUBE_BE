package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.dto.GuestbookResponseDto;
import com.roome.domain.guestbook.dto.PaginationDto;
import com.roome.domain.guestbook.service.GuestbookService;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class GuestbookControllerTest {

    @Mock
    private GuestbookService guestbookService;

    @InjectMocks
    private GuestbookController guestbookController;

    private Long roomId = 1L;
    private Long userId = 1L;
    private Long guestbookId = 1L;

    @BeforeEach
    public void setUp() {
        // 초기화 작업, 예를 들어 데이터베이스나 서비스 객체 등이 필요할 경우
    }

    @Test
    @DisplayName("방명록을 조회할 수 있다")
    public void testGetGuestbook_Success() {
        // given
        GuestbookListResponseDto guestbookListResponseDto = new GuestbookListResponseDto(
                roomId, Collections.singletonList(new GuestbookResponseDto(guestbookId, userId, "User", "profile.jpg", "Great room!", LocalDateTime.now(), "하우스메이트")),
                new PaginationDto(1, 10, 1)
        );
        when(guestbookService.getGuestbook(roomId, 1, 10)).thenReturn(guestbookListResponseDto);

        // when
        ResponseEntity<GuestbookListResponseDto> response = guestbookController.getGuestbook(roomId, 1, 10);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(roomId, response.getBody().getRoomId());
    }

    @Test
    @DisplayName("존재하지 않는 방에 대한 방명록 조회 시 예외가 발생한다")
    public void testGetGuestbook_RoomNotFound() {
        // given
        when(guestbookService.getGuestbook(roomId, 1, 10)).thenThrow(new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookController.getGuestbook(roomId, 1, 10));

        // then
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방명록을 추가할 수 있다")
    public void testAddGuestbook_Success() {
        // given
        GuestbookRequestDto requestDto = new GuestbookRequestDto("Nice place!");
        GuestbookResponseDto guestbookResponseDto = new GuestbookResponseDto(guestbookId, userId, "User", "profile.jpg", "Nice place!", LocalDateTime.now(), "하우스메이트");
        when(guestbookService.addGuestbook(roomId, userId, requestDto)).thenReturn(guestbookResponseDto);

        // when
        ResponseEntity<GuestbookResponseDto> response = guestbookController.addGuestbook(roomId, userId, requestDto);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Nice place!", response.getBody().getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 방에 방명록을 추가할 수 없다")
    public void testAddGuestbook_RoomNotFound() {
        // given
        GuestbookRequestDto requestDto = new GuestbookRequestDto("Nice place!");
        when(guestbookService.addGuestbook(roomId, userId, requestDto)).thenThrow(new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookController.addGuestbook(roomId, userId, requestDto));

        // then
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 사용자에 대해 방명록을 추가할 수 없다")
    public void testAddGuestbook_UserNotFound() {
        // given
        GuestbookRequestDto requestDto = new GuestbookRequestDto("Nice place!");
        when(guestbookService.addGuestbook(roomId, 999L, requestDto)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookController.addGuestbook(roomId, 999L, requestDto));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("방명록을 삭제할 수 있다")
    public void testDeleteGuestbook_Success() {
        // when
        ResponseEntity<Void> response = guestbookController.deleteGuestbook(guestbookId, userId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(guestbookService, times(1)).deleteGuestbook(guestbookId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 방명록을 삭제할 수 없다")
    public void testDeleteGuestbook_GuestbookNotFound() {
        Long guestbookId = 999L;
        Long userId = 1L;

        // given
        doThrow(new BusinessException(ErrorCode.GUESTBOOK_NOT_FOUND))
                .when(guestbookService).deleteGuestbook(guestbookId, userId);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookController.deleteGuestbook(guestbookId, userId));

        // then
        assertEquals(ErrorCode.GUESTBOOK_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("사용자가 권한이 없으면 방명록을 삭제할 수 없다")
    public void testDeleteGuestbook_UserNotAuthorized() {
        // given
        doThrow(new BusinessException(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN))
                .when(guestbookService).deleteGuestbook(guestbookId, userId);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> guestbookController.deleteGuestbook(guestbookId, userId));

        // then
        assertEquals(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN, exception.getErrorCode());
    }

}
