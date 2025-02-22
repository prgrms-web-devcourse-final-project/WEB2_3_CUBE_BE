package com.roome.domain.user.service;

import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private HousemateRepository housemateRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private FurnitureRepository furnitureRepository;
    @Mock
    private MyBookRepository myBookRepository;
    @Mock
    private MyBookReviewRepository myBookReviewRepository;
    @Mock
    private MyCdRepository myCdRepository;
    @Mock
    private CdCommentRepository cdCommentRepository;
    @Mock
    private GuestbookRepository guestbookRepository;
    @Mock
    private MyCdCountRepository myCdCountRepository;
    @Mock
    private MyBookCountRepository myBookCountRepository;

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .user(testUser)
                .build();

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(roomRepository.findByUserId(1L)).thenReturn(Optional.of(testRoom));
    }

    @Test
    void 회원탈퇴_성공() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void 존재하지_않는_사용자_탈퇴_실패() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.deleteUser(1L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
