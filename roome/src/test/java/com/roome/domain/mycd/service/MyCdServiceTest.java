package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.repository.CdGenreTypeRepository;
import com.roome.domain.cd.repository.CdRepository;
import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.exception.MyCdAlreadyExistsException;
import com.roome.domain.mycd.exception.MyCdListEmptyException;
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MyCdServiceTest {

  private MyCdRepository myCdRepository;
  private CdRepository cdRepository;
  private MyCdCountRepository myCdCountRepository;
  private RoomRepository roomRepository;
  private UserRepository userRepository;
  private CdGenreTypeRepository cdGenreTypeRepository;
  private MyCdService myCdService;

  @BeforeEach
  void setUp() {
    myCdRepository = mock(MyCdRepository.class);
    cdRepository = mock(CdRepository.class);
    myCdCountRepository = mock(MyCdCountRepository.class);
    roomRepository = mock(RoomRepository.class);
    userRepository = mock(UserRepository.class);
    cdGenreTypeRepository = mock(CdGenreTypeRepository.class);
    myCdService = new MyCdService(myCdRepository, cdRepository, myCdCountRepository, roomRepository, userRepository, cdGenreTypeRepository);
  }

  @Test
  @DisplayName("CD 추가 성공")
  void addCdToMyList_Success() {
    Long userId = 1L;
    MyCdCreateRequest request = new MyCdCreateRequest("Palette", "IU", "Palette",
        LocalDate.of(2019, 11, 1),
        List.of("K-Pop", "Ballad"), "https://example.com/image1.jpg",
        "https://youtube.com/watch?v=asdf5678", 215);

    User user = mock(User.class);
    Room room = mock(Room.class);
    Cd cd = mock(Cd.class);
    MyCd myCd = mock(MyCd.class);
    MyCdCount myCdCount = mock(MyCdCount.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));
    when(cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist())).thenReturn(Optional.of(cd));
    when(myCdRepository.existsByUserIdAndCdId(userId, 1L)).thenReturn(false);
    when(myCdRepository.save(any(MyCd.class))).thenReturn(myCd);
    when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));
    when(myCd.getCd()).thenReturn(cd);
    when(cd.getId()).thenReturn(1L);

    MyCdResponse response = myCdService.addCdToMyList(userId, request);

    assertThat(response).isNotNull();
    verify(myCdRepository, times(1)).save(any(MyCd.class));
  }

  @Test
  @DisplayName("CD 추가 실패 - 중복된 CD")
  void addCdToMyList_Failure_AlreadyExists() {
    // Given
    Long userId = 1L;
    MyCdCreateRequest request = new MyCdCreateRequest("Palette", "IU", "Palette",
        LocalDate.of(2019, 11, 1),
        List.of("K-Pop", "Ballad"), "https://example.com/image1.jpg",
        "https://youtube.com/watch?v=asdf5678", 215);

    Cd cd = mock(Cd.class);
    User user = mock(User.class);
    Room room = mock(Room.class);
    MyCd myCd = mock(MyCd.class);
    MyCdCount myCdCount = mock(MyCdCount.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));
    when(cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist()))
        .thenReturn(Optional.of(cd));
    when(myCdRepository.existsByUserIdAndCdId(userId, 1L)).thenReturn(true);
    when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));

    when(myCdRepository.save(any(MyCd.class))).thenReturn(myCd);

    when(myCd.getCd()).thenReturn(cd);
    when(cd.getId()).thenReturn(1L);

    // When & Then
    assertThatThrownBy(() -> myCdService.addCdToMyList(userId, request))
        .isInstanceOf(MyCdAlreadyExistsException.class);
  }

  @Test
  @DisplayName("내 CD 목록 조회 성공")
  void getMyCdList_Success() {
    Long userId = 1L;
    PageRequest pageRequest = PageRequest.of(0, 10);

    User user = mock(User.class);
    Cd cd = mock(Cd.class);
    MyCd myCd = mock(MyCd.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(myCdRepository.findByUserIdOrderByIdAsc(userId, pageRequest))
        .thenReturn(List.of(myCd));

    when(myCd.getCd()).thenReturn(cd);
    when(myCd.getId()).thenReturn(1L);
    when(cd.getId()).thenReturn(1L);
    when(cd.getTitle()).thenReturn("Palette");
    when(cd.getArtist()).thenReturn("IU");
    when(cd.getAlbum()).thenReturn("Palette");
    when(cd.getCoverUrl()).thenReturn("https://example.com/image1.jpg");
    when(cd.getYoutubeUrl()).thenReturn("https://youtube.com/watch?v=asdf5678");

    MyCdListResponse response = myCdService.getMyCdList(userId, null, 10);

    assertThat(response).isNotNull();
    assertThat(response.getData()).hasSize(1);
  }

  @Test
  @DisplayName("내 CD 목록 조회 실패 - 목록 없음")
  void getMyCdList_Failure_Empty() {
    Long userId = 1L;
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
    when(myCdRepository.findByUserIdOrderByIdAsc(userId, pageRequest)).thenReturn(List.of());

    assertThatThrownBy(() -> myCdService.getMyCdList(userId, null, 10))
        .isInstanceOf(MyCdListEmptyException.class);
  }

  @Test
  @DisplayName("CD 삭제 성공")
  void delete_Success() {
    Long userId = 1L;
    List<Long> ids = List.of(1L, 2L, 3L);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
    when(myCdRepository.findAllById(ids)).thenReturn(List.of(mock(MyCd.class), mock(MyCd.class)));

    myCdService.delete(userId, "1,2,3");

    verify(myCdRepository, times(1)).deleteByUserIdAndIds(userId, ids);
  }

  @Test
  @DisplayName("CD 삭제 실패 - 존재하지 않음")
  void delete_Failure_NotFound() {
    Long userId = 1L;
    List<Long> ids = List.of(1L, 2L, 3L);

    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
    when(myCdRepository.findAllById(ids)).thenReturn(List.of());

    assertThatThrownBy(() -> myCdService.delete(userId, "1,2,3"))
        .isInstanceOf(MyCdNotFoundException.class);
  }
}
