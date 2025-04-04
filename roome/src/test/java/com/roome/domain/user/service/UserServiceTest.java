package com.roome.domain.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.cdcomment.entity.CdComment;
import com.roome.domain.cdcomment.repository.CdCommentRepository;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
  private PointHistoryRepository pointHistoryRepository;
  @Mock
  private PointRepository pointRepository;
  @Mock
  private MyBookCountRepository myBookCountRepository; // 추가

  private static final Long USER_ID = 1L;
  private static final Long ROOM_ID = 1L;

  private User testUser;
  private Room testRoom;

  @BeforeEach
  void setUp() {
    testUser = mock(User.class);
    testRoom = mock(Room.class);

    when(testUser.getId()).thenReturn(USER_ID);
    when(testRoom.getId()).thenReturn(ROOM_ID);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
    when(roomRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testRoom));
    when(furnitureRepository.findByRoomId(ROOM_ID)).thenReturn(Collections.emptyList());
    when(myBookCountRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
  }

  @Test
  @DisplayName("회원 탈퇴 성공 - 모든 연관 데이터 삭제")
  void deleteUserSuccess() {
    // given
    List<Furniture> furnitures = List.of(mock(Furniture.class), mock(Furniture.class));
    List<MyBook> myBooks = List.of(mock(MyBook.class));
    List<MyCd> myCds = List.of(mock(MyCd.class), mock(MyCd.class));
    List<CdComment> comments = List.of(mock(CdComment.class));
    List<Guestbook> guestbooks = List.of(mock(Guestbook.class), mock(Guestbook.class));

    when(furnitureRepository.findByRoomId(ROOM_ID)).thenReturn(furnitures);
    when(myBookRepository.findAllByUserId(USER_ID)).thenReturn(myBooks);
    when(myCdRepository.findByUserId(USER_ID)).thenReturn(myCds);
    when(cdCommentRepository.findAllByUserId(USER_ID)).thenReturn(comments);
    when(guestbookRepository.findAllByRoomOrUserId(testRoom, USER_ID)).thenReturn(guestbooks);
    when(housemateRepository.deleteByUserIdOrAddedId(USER_ID, USER_ID)).thenReturn(3);

    // when
    userService.deleteUser(USER_ID);

    // then
    verify(housemateRepository, times(1)).deleteByUserIdOrAddedId(USER_ID, USER_ID);
    verify(myBookReviewRepository, times(1)).deleteAllByUserId(USER_ID);
    verify(myBookRepository, times(1)).deleteAll(myBooks);
    verify(cdCommentRepository, times(1)).deleteAll(comments);
    verify(myCdRepository, times(1)).deleteAll(myCds);
    verify(guestbookRepository, times(1)).deleteAll(guestbooks);
    for (Furniture furniture : furnitures) {
      verify(furnitureRepository, times(1)).delete(furniture);
    }
    verify(roomRepository, times(1)).delete(testRoom);
    verify(userRepository, times(1)).delete(testUser);
  }

  @Test
  @DisplayName("회원 탈퇴 성공 - 빈 컬렉션 처리")
  void deleteUserSuccessWithEmptyCollections() {
    // given
    when(roomRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testRoom));
    when(furnitureRepository.findByRoomId(ROOM_ID)).thenReturn(Collections.emptyList());
    when(myBookRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(myCdRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(cdCommentRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(guestbookRepository.findAllByRoomOrUserId(testRoom, USER_ID)).thenReturn(
        Collections.emptyList());

    // when
    userService.deleteUser(USER_ID);

    // then
    verify(userRepository, times(1)).delete(testUser);
    verify(roomRepository, times(1)).delete(testRoom);
  }

  @Test
  @DisplayName("회원 탈퇴 시 사용자를 찾을 수 없는 경우")
  void deleteUserWithNonExistentUser() {
    // given
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.deleteUser(USER_ID)).isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

    verify(userRepository, never()).delete(any());
  }

  @Test
  @DisplayName("회원 탈퇴 시 방이 없는 경우에도 성공")
  void deleteUserSuccessWithNoRoom() {
    // given
    when(roomRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

    // when
    userService.deleteUser(USER_ID);

    // then
    verify(roomRepository, never()).delete(any());
    verify(userRepository, times(1)).delete(testUser);
  }

  @Test
  @DisplayName("회원 탈퇴 시 하위 단계에서 예외 발생 시 전체 롤백")
  void deleteUserFailsAndRollsBackWhenSubOperationFails() {
    // given
    doThrow(new RuntimeException("Database error")).when(housemateRepository)
        .deleteByUserIdOrAddedId(USER_ID, USER_ID);

    // when & then
    assertThatThrownBy(() -> userService.deleteUser(USER_ID)).isInstanceOf(RuntimeException.class);

    verify(userRepository, never()).delete(any());
  }
}
