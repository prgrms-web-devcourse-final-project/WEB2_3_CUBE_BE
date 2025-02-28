//package com.roome.domain.mycd.service;
//
//import com.roome.domain.cd.entity.Cd;
//import com.roome.domain.cd.repository.CdGenreTypeRepository;
//import com.roome.domain.cd.repository.CdRepository;
//import com.roome.domain.mycd.dto.MyCdCreateRequest;
//import com.roome.domain.mycd.dto.MyCdListResponse;
//import com.roome.domain.mycd.dto.MyCdResponse;
//import com.roome.domain.mycd.entity.MyCd;
//import com.roome.domain.mycd.entity.MyCdCount;
//import com.roome.domain.mycd.exception.MyCdAlreadyExistsException;
//import com.roome.domain.mycd.exception.MyCdListEmptyException;
//import com.roome.domain.mycd.exception.MyCdNotFoundException;
//import com.roome.domain.mycd.repository.MyCdCountRepository;
//import com.roome.domain.mycd.repository.MyCdRepository;
//import com.roome.domain.room.entity.Room;
//import com.roome.domain.room.repository.RoomRepository;
//import com.roome.domain.user.entity.User;
//import com.roome.domain.user.repository.UserRepository;
//import java.time.LocalDate;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.PageRequest;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.*;
//
//class MyCdServiceTest {
//
//  private MyCdRepository myCdRepository;
//  private CdRepository cdRepository;
//  private MyCdCountRepository myCdCountRepository;
//  private RoomRepository roomRepository;
//  private UserRepository userRepository;
//  private CdGenreTypeRepository cdGenreTypeRepository;
//  private MyCdService myCdService;
//
//  @BeforeEach
//  void setUp() {
//    myCdRepository = mock(MyCdRepository.class);
//    cdRepository = mock(CdRepository.class);
//    myCdCountRepository = mock(MyCdCountRepository.class);
//    roomRepository = mock(RoomRepository.class);
//    userRepository = mock(UserRepository.class);
//    cdGenreTypeRepository = mock(CdGenreTypeRepository.class);
//    myCdService = new MyCdService(myCdRepository, cdRepository, myCdCountRepository, roomRepository,
//        userRepository, cdGenreTypeRepository);
//  }
//
//  @Test
//  @DisplayName("새로운 CD 추가 성공")
//  void addCdToMyList_Success_NewCd() {
//    Long userId = 1L;
//    MyCdCreateRequest request = new MyCdCreateRequest(
//        "New Album", "New Artist", "New Album",
//        LocalDate.of(2022, 1, 1),
//        List.of("Pop"), "https://example.com/new_cd.jpg",
//        "https://youtube.com/watch?v=newvideo", 200
//    );
//
//    User user = mock(User.class);
//    Room room = mock(Room.class);
//    Cd newCd = mock(Cd.class);
//    MyCd myCd = mock(MyCd.class);
//    MyCdCount myCdCount = mock(MyCdCount.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//    when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));
//    when(cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist()))
//        .thenReturn(Optional.empty());
//    when(cdRepository.save(any(Cd.class))).thenReturn(newCd);
//    when(myCdRepository.save(any(MyCd.class))).thenReturn(myCd);
//    when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));
//
//    when(myCd.getCd()).thenReturn(newCd);
//    when(newCd.getTitle()).thenReturn("New Album");
//    when(newCd.getArtist()).thenReturn("New Artist");
//
//    MyCdResponse response = myCdService.addCdToMyList(userId, request);
//
//    assertThat(response).isNotNull();
//    assertThat(response.getTitle()).isEqualTo("New Album");
//    assertThat(response.getArtist()).isEqualTo("New Artist");
//
//    verify(cdRepository, times(1)).save(any(Cd.class));
//  }
//
//  @Test
//  @DisplayName("CD 추가 실패 - 중복된 CD")
//  void addCdToMyList_Failure_AlreadyExists() {
//    Long userId = 1L;
//    MyCdCreateRequest request = new MyCdCreateRequest("Palette", "IU", "Palette",
//        LocalDate.of(2019, 11, 1),
//        List.of("K-Pop", "Ballad"), "https://example.com/image1.jpg",
//        "https://youtube.com/watch?v=asdf5678", 215);
//
//    Cd cd = mock(Cd.class);
//    User user = mock(User.class);
//    Room room = mock(Room.class);
//    MyCd myCd = mock(MyCd.class);
//    MyCdCount myCdCount = mock(MyCdCount.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//    when(roomRepository.findByUserId(userId)).thenReturn(Optional.of(room));
//    when(cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist()))
//        .thenReturn(Optional.of(cd));
//    when(myCdRepository.existsByUserIdAndCdId(userId, 1L)).thenReturn(true);
//    when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));
//
//    when(myCdRepository.save(any(MyCd.class))).thenReturn(myCd);
//
//    when(myCd.getCd()).thenReturn(cd);
//    when(cd.getId()).thenReturn(1L);
//
//    assertThatThrownBy(() -> myCdService.addCdToMyList(userId, request))
//        .isInstanceOf(MyCdAlreadyExistsException.class);
//  }
//
//  @Test
//  @DisplayName("내 CD 목록 조회 - 커서 기반 페이징 성공")
//  void getMyCdList_WithCursor_Success() {
//    Long userId = 1L;
//    Long cursor = 5L;
//    PageRequest pageRequest = PageRequest.of(0, 10);
//
//    User user = mock(User.class);
//    Cd cd = mock(Cd.class);
//    MyCd myCd = mock(MyCd.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//    when(myCdRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, cursor, pageRequest))
//        .thenReturn(List.of(myCd));
//
//    when(myCd.getCd()).thenReturn(cd);
//    when(myCd.getId()).thenReturn(1L);
//    when(cd.getId()).thenReturn(1L);
//    when(cd.getTitle()).thenReturn("Palette");
//    when(cd.getArtist()).thenReturn("IU");
//
//    MyCdListResponse response = myCdService.getMyCdList(userId, null, cursor, 10);
//
//    assertThat(response).isNotNull();
//    assertThat(response.getData()).hasSize(1);
//  }
//
//  @Test
//  @DisplayName("내 CD 목록 조회 - 키워드 검색 성공")
//  void getMyCdList_WithKeyword_Success() {
//    Long userId = 1L;
//    String keyword = "IU";
//    PageRequest pageRequest = PageRequest.of(0, 10);
//
//    User user = mock(User.class);
//    Cd cd = mock(Cd.class);
//    MyCd myCd = mock(MyCd.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//    when(myCdRepository.searchByUserIdAndKeyword(eq(userId), eq(keyword), any(PageRequest.class)))
//        .thenReturn(List.of(myCd));
//
//    when(myCdRepository.countByUserIdAndKeyword(eq(userId), eq(keyword)))
//        .thenReturn(1L);
//
//    when(myCd.getCd()).thenReturn(cd);
//    when(myCd.getId()).thenReturn(1L);
//    when(cd.getId()).thenReturn(1L);
//    when(cd.getTitle()).thenReturn("Palette");
//    when(cd.getArtist()).thenReturn("IU");
//
//    MyCdListResponse response = myCdService.getMyCdList(userId, keyword, null, 10);
//
//    assertThat(response).isNotNull();
//    assertThat(response.getData()).hasSize(1);
//  }
//
//  @Test
//  @DisplayName("내 CD 목록 조회 - 키워드 검색 실패 (결과 없음)")
//  void getMyCdList_WithKeyword_Failure() {
//    Long userId = 1L;
//    String keyword = "Unknown Artist";
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//
//    when(myCdRepository.searchByUserIdAndKeyword(eq(userId), eq(keyword), any(PageRequest.class)))
//        .thenReturn(List.of());
//
//    when(myCdRepository.countByUserIdAndKeyword(eq(userId), eq(keyword))).thenReturn(0L);
//
//    assertThatThrownBy(() -> myCdService.getMyCdList(userId, keyword, null, 10))
//        .isInstanceOf(MyCdListEmptyException.class);
//  }
//
//  @Test
//  @DisplayName("내 CD 목록 조회 실패 - 보유한 CD 없음")
//  void getMyCdList_Failure_Empty() {
//    Long userId = 1L;
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//    when(myCdRepository.findByUserIdOrderByIdAsc(userId, PageRequest.of(0, 10)))
//        .thenReturn(List.of());
//
//    assertThatThrownBy(() -> myCdService.getMyCdList(userId, null, null, 10))
//        .isInstanceOf(MyCdListEmptyException.class);
//  }
//
//
//  @Test
//  @DisplayName("내 CD 목록 조회 - cursor 없음")
//  void getMyCdList_Success_NoCursor() {
//    Long userId = 1L;
//    PageRequest pageRequest = PageRequest.of(0, 10);
//    MyCd myCd = mock(MyCd.class);
//    Cd cd = mock(Cd.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//    when(myCdRepository.findByUserIdOrderByIdAsc(userId, pageRequest))
//        .thenReturn(List.of(myCd));
//
//    when(myCd.getCd()).thenReturn(cd);
//    when(myCd.getId()).thenReturn(1L);
//
//    MyCdListResponse response = myCdService.getMyCdList(userId, null, null, 10);
//    assertThat(response).isNotNull();
//    assertThat(response.getData()).hasSize(1);
//  }
//
//  @Test
//  @DisplayName("CD 단건 조회 - 존재하는 CD 조회 성공")
//  void getMyCd_Success() {
//    Long userId = 1L;
//    Long myCdId = 1L;
//
//    User user = mock(User.class);
//    Cd cd = mock(Cd.class);
//    MyCd myCd = mock(MyCd.class);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//    when(myCdRepository.findByIdAndUserId(myCdId, userId)).thenReturn(Optional.of(myCd));
//
//    when(myCd.getCd()).thenReturn(cd);
//    when(myCd.getId()).thenReturn(1L);
//    when(cd.getId()).thenReturn(1L);
//    when(cd.getTitle()).thenReturn("Palette");
//    when(cd.getArtist()).thenReturn("IU");
//
//    MyCdResponse response = myCdService.getMyCd(userId, myCdId);
//
//    assertThat(response).isNotNull();
//    assertThat(response.getTitle()).isEqualTo("Palette");
//  }
//
//
//  @Test
//  @DisplayName("CD 단건 조회 실패 - 존재하지 않음")
//  void getMyCd_Failure_NotFound() {
//    Long userId = 1L;
//    Long myCdId = 999L;
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//    when(myCdRepository.findByIdAndUserId(myCdId, userId)).thenReturn(Optional.empty());
//
//    assertThatThrownBy(() -> myCdService.getMyCd(userId, myCdId))
//        .isInstanceOf(MyCdNotFoundException.class);
//  }
//
//  @Test
//  @DisplayName("CD 삭제 성공")
//  void delete_Success() {
//    Long userId = 1L;
//    List<Long> ids = List.of(1L, 2L, 3L);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//    when(myCdRepository.findAllById(ids)).thenReturn(List.of(mock(MyCd.class), mock(MyCd.class)));
//
//    myCdService.delete(userId, ids);
//
//    verify(myCdRepository, times(1)).deleteByUserIdAndIds(userId, ids);
//  }
//
//  @Test
//  @DisplayName("CD 삭제 실패 - 존재하지 않음")
//  void delete_Failure_NotFound() {
//    Long userId = 1L;
//    List<Long> ids = List.of(1L, 2L, 3L);
//
//    when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
//    when(myCdRepository.findAllById(ids)).thenReturn(List.of());
//
//    assertThatThrownBy(() -> myCdService.delete(userId, ids))
//        .isInstanceOf(MyCdNotFoundException.class);
//  }
//}
