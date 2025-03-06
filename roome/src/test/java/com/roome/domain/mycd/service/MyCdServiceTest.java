package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.repository.CdGenreTypeRepository;
import com.roome.domain.cd.repository.CdRepository;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureCapacity;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.furniture.service.FurnitureService;
import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.exception.MyCdListEmptyException;
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyCdServiceTest {

  @Mock
  private MyCdRepository myCdRepository;
  @Mock
  private CdRepository cdRepository;
  @Mock
  private RoomRepository roomRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private FurnitureRepository furnitureRepository;
  @Mock
  private FurnitureCapacity furnitureCapacity;
  @Mock
  private FurnitureType furnitureType;
  @Mock
  private CdGenreTypeRepository cdGenreTypeRepository;
  @Mock
  private MyCdCountRepository myCdCountRepository;
  @Mock
  private FurnitureService furnitureService;
  @Mock
  private UserActivityService userActivityService;
  @Mock
  private Room room;
  @Mock
  private Furniture furniture;

  @InjectMocks
  private MyCdService myCdService;

  private User user;
  private Cd cd;
  private MyCd myCd;

  @BeforeEach
  void setUp() {
    user = User.builder().id(1L).nickname("테스트 유저").build();
    room = Room.builder().id(1L).user(user).build();
    cd = Cd.builder().id(1L).title("Palette").artist("IU").build();
    myCd = MyCd.builder().id(1L).user(user).room(room).cd(cd).build();
  }

//  @Test
//  @DisplayName("CD 추가 성공 - CD 랙 레벨 확인 및 저장")
//  void addCdToMyList_Success() {
//    // Given
//    MyCdCreateRequest request = new MyCdCreateRequest(
//        "Palette", "IU", "Palette",
//        LocalDate.of(2019, 11, 1),
//        List.of("K-Pop", "Ballad"),
//        "https://example.com/image1.jpg",
//        "https://youtube.com/watch?v=asdf5678",
//        215
//    );
//
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(roomRepository.findByUserId(1L)).thenReturn(Optional.of(room));
//
//    // 가구 서비스 Mock 설정
//    Furniture furnitureMock = mock(Furniture.class); // Mock 객체 생성
//    when(furnitureMock.getLevel()).thenReturn(2); // `getLevel()`을 Mocking
//    when(furnitureRepository.findByRoomAndFurnitureType(any(Room.class), eq(FurnitureType.CD_RACK)))
//        .thenReturn(Optional.of(furnitureMock)); // Mock 객체 반환 보장
//
//    when(furnitureCapacity.getMaxCdCapacity(2)).thenReturn(50); // 최대 수용량 Mock
//
//    // 현재 등록된 CD 개수 (현재 10개로 설정)
//    when(myCdRepository.countByUserId(1L)).thenReturn(10L);
//
//    // CD 존재 여부 Mock 설정 (새로 생성해야 하는 경우)
//    when(cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist()))
//        .thenReturn(Optional.empty());
//
//    Cd newCd = mock(Cd.class);
//    when(newCd.getId()).thenReturn(99L); // 새 CD ID
//    when(cdRepository.save(any(Cd.class))).thenReturn(newCd);
//
//    // 중복 추가 방지 Mock 설정 (중복이 없도록 설정)
//    when(myCdRepository.findByUserIdAndCdId(1L, newCd.getId())).thenReturn(Optional.empty());
//
//    // MyCd 저장 Mock 설정
//    MyCd myCd = mock(MyCd.class);
//    when(myCd.getCd()).thenReturn(newCd);
//    when(myCdRepository.save(any(MyCd.class))).thenReturn(myCd);
//
//    // MyCdCount Mock 설정
//    MyCdCount myCdCount = mock(MyCdCount.class);
//    when(myCdCountRepository.findByRoom(room)).thenReturn(Optional.of(myCdCount));
//    doNothing().when(myCdCount).increment();
//
//    // 유저 활동 기록 Mock 설정
//    doNothing().when(userActivityService).recordUserActivity(1L, ActivityType.MUSIC_REGISTRATION, newCd.getId());
//
//    // When
//    MyCdResponse response = myCdService.addCdToMyList(1L, request);
//
//    // Then
//    assertThat(response).isNotNull();
//    assertThat(response.getTitle()).isEqualTo("Palette");
//    assertThat(response.getArtist()).isEqualTo("IU");
//
//    // 저장 메서드들이 제대로 호출되었는지 검증
//    verify(cdRepository, times(1)).save(any(Cd.class));
//    verify(myCdRepository, times(1)).save(any(MyCd.class));
//    verify(myCdCount, times(1)).increment();
//    verify(userActivityService, times(1)).recordUserActivity(1L, ActivityType.MUSIC_REGISTRATION, newCd.getId());
//  }

  @Test
  @DisplayName("내 CD 목록 조회 성공 - 키워드 없음, 커서 없음")
  @WithMockUser(username = "1")
  void getMyCdList_Success_NoKeywordNoCursor() throws Exception {
    PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

    when(myCdRepository.findByUserIdOrderByIdAsc(eq(1L), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(myCd), pageRequest, 1));

    // 첫 번째 & 마지막 CD ID 조회 Mock 추가
    when(myCdRepository.findFirstByUserIdOrderByIdAsc(1L)).thenReturn(Optional.of(myCd));
    when(myCdRepository.findFirstByUserIdOrderByIdDesc(1L)).thenReturn(Optional.of(myCd));

    MyCdListResponse response = myCdService.getMyCdList(1L, null, null, 10);

    assertThat(response).isNotNull();
    assertThat(response.getData()).hasSize(1);
  }

  @Test
  @DisplayName("내 CD 목록 조회 성공 - 키워드 검색 포함")
  @WithMockUser(username = "1")
  void getMyCdList_Success_WithKeyword() throws Exception {
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(myCdRepository.searchByUserIdAndKeyword(eq(1L), eq("IU"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(myCd), pageRequest, 1));

    // 첫 번째 & 마지막 CD ID 조회 Mock 추가
    when(myCdRepository.findFirstByUserIdOrderByIdAsc(1L)).thenReturn(Optional.of(myCd));
    when(myCdRepository.findFirstByUserIdOrderByIdDesc(1L)).thenReturn(Optional.of(myCd));

    MyCdListResponse response = myCdService.getMyCdList(1L, "IU", null, 10);

    assertThat(response).isNotNull();
    assertThat(response.getData()).hasSize(1);
  }

  @Test
  @DisplayName("내 CD 목록 조회 실패 - 결과 없음")
  void getMyCdList_Failure_Empty() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(myCdRepository.findByUserIdOrderByIdAsc(eq(1L), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

    assertThatThrownBy(() -> myCdService.getMyCdList(1L, null, null, 10))
        .isInstanceOf(MyCdListEmptyException.class);
  }

  @Test
  @DisplayName("CD 단건 조회 성공")
  void getMyCd_Success() {
    when(myCdRepository.findByIdAndUserId(eq(1L), eq(1L)))
        .thenReturn(Optional.of(myCd));

    MyCdResponse response = myCdService.getMyCd(1L, 1L);

    assertThat(response).isNotNull();
    assertThat(response.getTitle()).isEqualTo("Palette");
  }

  @Test
  @DisplayName("CD 단건 조회 실패 - 존재하지 않음")
  void getMyCd_Failure_NotFound() {
    when(myCdRepository.findByIdAndUserId(eq(999L), eq(1L)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> myCdService.getMyCd(1L, 999L))
        .isInstanceOf(MyCdNotFoundException.class);
  }

  @Test
  @DisplayName("CD 삭제 성공")
  void delete_Success() {
    List<Long> ids = List.of(1L, 2L, 3L);
    when(myCdRepository.findAllById(ids)).thenReturn(List.of(myCd, myCd));

    myCdService.delete(1L, ids);

    verify(myCdRepository, times(1)).deleteByUserIdAndIds(1L, ids);
  }

  @Test
  @DisplayName("CD 삭제 실패 - 존재하지 않음")
  void delete_Failure_NotFound() {
    List<Long> ids = List.of(1L, 2L, 3L);
    when(myCdRepository.findAllById(ids)).thenReturn(List.of());

    assertThatThrownBy(() -> myCdService.delete(1L, ids))
        .isInstanceOf(MyCdNotFoundException.class);
  }
}
