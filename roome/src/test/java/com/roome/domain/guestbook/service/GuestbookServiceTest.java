package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.dto.PaginationDto;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestbookServiceTest {

    @Mock
    private GuestbookRepository guestbookRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GuestbookService guestbookService;

    private Room room;
    private User user;
    private Guestbook guestbook;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "nickname", "John");
        ReflectionTestUtils.setField(user, "profileImage", "profile.jpg");

        room = Room.builder()
                .user(user)
                .furnitures(List.of())
                .theme(RoomTheme.BASIC)
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(room, "id", 1L);

        guestbook = Guestbook.builder()
                .room(room)
                .user(user)
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .message("Test guestbook message")
                .relation(RelationType.지나가던_나그네)
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(guestbook, "guestbookId", 1L);
    }

    @Test
    void testGetGuestbook_Success() {
        Long roomId = 1L;
        int page = 1;
        int size = 10;

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Page<Guestbook> guestbookPage = new PageImpl<>(List.of(guestbook), PageRequest.of(page - 1, size), 1);
        when(guestbookRepository.findByRoom(room, PageRequest.of(page - 1, size))).thenReturn(guestbookPage);

        GuestbookListResponseDto responseDto = guestbookService.getGuestbook(roomId, page, size);
        assertNotNull(responseDto);
        assertEquals(roomId, responseDto.getRoomId());
        assertNotNull(responseDto.getGuestbook());
        assertEquals(1, responseDto.getGuestbook().size());

        GuestbookResponseDto entry = responseDto.getGuestbook().get(0);
        assertEquals(1L, entry.getGuestbookId());
        assertEquals("Test guestbook message", entry.getMessage());

        PaginationDto pagination = responseDto.getPagination();
        assertNotNull(pagination);
        assertEquals(page, pagination.getPage());
        assertEquals(size, pagination.getSize());
        assertEquals(1, pagination.getTotalPages());

        verify(roomRepository).findById(roomId);
        verify(guestbookRepository).findByRoom(room, PageRequest.of(page - 1, size));
    }

    @Test
    void testGetGuestbook_RoomNotFound() {
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> guestbookService.getGuestbook(roomId, 1, 10));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testAddGuestbook_Success() {
        Long roomId = 1L;
        GuestbookRequestDto requestDto = new GuestbookRequestDto();

        ReflectionTestUtils.setField(requestDto, "message", "Test guestbook message");

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(guestbookRepository.save(any(Guestbook.class))).thenAnswer(invocation -> {
            Guestbook saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "guestbookId", 1L);
            return saved;
        });

        GuestbookResponseDto responseDto = guestbookService.addGuestbook(roomId, user, requestDto);
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getGuestbookId());
        assertEquals(user.getId(), responseDto.getUserId());
        assertEquals("Test guestbook message", responseDto.getMessage());

        assertEquals(guestbook.getRelation().name(), responseDto.getRelation());

        verify(roomRepository).findById(roomId);
        verify(guestbookRepository).save(any(Guestbook.class));
    }

    @Test
    void testAddGuestbook_RoomNotFound() {
        Long roomId = 1L;
        GuestbookRequestDto requestDto = new GuestbookRequestDto();
        ReflectionTestUtils.setField(requestDto, "message", "Test guestbook message");

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        BusinessException exception = assertThrows(BusinessException.class,
                () -> guestbookService.addGuestbook(roomId, user, requestDto));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDeleteGuestbook_Success() {
        Long guestbookId = 1L;
        when(guestbookRepository.findById(guestbookId)).thenReturn(Optional.of(guestbook));

        guestbookService.deleteGuestbook(guestbookId, user);
        verify(guestbookRepository).findById(guestbookId);
        verify(guestbookRepository).delete(guestbook);
    }

    @Test
    void testDeleteGuestbook_GuestbookNotFound() {
        Long guestbookId = 1L;
        when(guestbookRepository.findById(guestbookId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> guestbookService.deleteGuestbook(guestbookId, user));
        assertEquals(ErrorCode.GUESTBOOK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDeleteGuestbook_DeleteForbidden() {
        Long guestbookId = 1L;

        User otherUser = User.builder().build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        when(guestbookRepository.findById(guestbookId)).thenReturn(Optional.of(guestbook));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> guestbookService.deleteGuestbook(guestbookId, otherUser));
        assertEquals(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN, exception.getErrorCode());
    }
}
