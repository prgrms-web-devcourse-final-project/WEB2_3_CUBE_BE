package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.entity.CdGenre;
import com.roome.domain.cd.entity.CdGenreType;
import com.roome.domain.cd.repository.CdGenreTypeRepository;
import com.roome.domain.cd.repository.CdRepository;
import com.roome.domain.furniture.entity.FurnitureCapacity;
import com.roome.domain.furniture.service.FurnitureService;
import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.event.CdCollectionEvent;
import com.roome.domain.mycd.exception.CdRackCapacityExceededException;
import com.roome.domain.mycd.exception.MyCdAlreadyExistsException;
import com.roome.domain.mycd.exception.MyCdDatabaseException;
import com.roome.domain.mycd.exception.MyCdListEmptyException;
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.exception.MyCdPaginationException;
import com.roome.domain.mycd.exception.MyCdUnauthorizedException;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomNoFoundException;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyCdService {

  private final MyCdRepository myCdRepository;
  private final CdRepository cdRepository;
  private final MyCdCountRepository myCdCountRepository;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final CdGenreTypeRepository cdGenreTypeRepository;
  private final UserActivityService userActivityService;
  private final FurnitureService furnitureService;
  private final FurnitureCapacity furnitureCapacity;
  private final ApplicationEventPublisher eventPublisher; // 이벤트 발행을 위해 추가

  public MyCdResponse addCdToMyList(Long userId, MyCdCreateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    Room room = roomRepository.findByUserId(userId).orElseThrow(RoomNoFoundException::new);

    // CD_RACK 레벨 확인
    int cdRackLevel = furnitureService.getCdRackLevel(room);
    int maxCapacity = furnitureCapacity.getMaxCdCapacity(cdRackLevel);

    // 현재 등록된 CD 개수 조회
    long currentCdCount = myCdRepository.countByUserId(userId);
    if (currentCdCount >= maxCapacity) {
      throw new CdRackCapacityExceededException();
    }

    // CD 존재 여부 확인 후 저장 (없으면 새로 생성)
    Cd cd = cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist())
        .orElseGet(() -> {
          Cd newCd = Cd.create(request.getTitle(), request.getArtist(), request.getAlbum(),
              request.getReleaseDate(), request.getCoverUrl(), request.getYoutubeUrl(),
              request.getDuration());

          for (String genreName : request.getGenres()) {
            CdGenreType genreType = cdGenreTypeRepository.findByName(genreName)
                .orElseGet(() -> cdGenreTypeRepository.save(new CdGenreType(genreName)));

            CdGenre cdGenre = new CdGenre(newCd, genreType);
            newCd.addGenre(cdGenre);
          }

          return cdRepository.save(newCd);
        });

    // 중복 추가 방지
    myCdRepository.findByUserIdAndCdId(userId, cd.getId())
        .ifPresent(existingCd -> {
          throw new MyCdAlreadyExistsException();
        });

    // MyCd 저장
    MyCd myCd = myCdRepository.save(MyCd.create(user, room, cd));

    // MyCdCount 업데이트
    MyCdCount myCdCount = myCdCountRepository.findByRoom(room)
        .orElseGet(() -> myCdCountRepository.save(MyCdCount.init(room)));
    myCdCount.increment();

    // 음악 등록 활동 기록 추가
    userActivityService.recordUserActivity(userId, ActivityType.MUSIC_REGISTRATION, cd.getId());
    // 이벤트 발행
    eventPublisher.publishEvent(new CdCollectionEvent.CdAddedEvent(this, userId));
    log.debug("Published CD added event for user: {}", userId);
    return MyCdResponse.fromEntity(myCd);
  }

  public MyCdListResponse getMyCdList(Long userId, String keyword, Long cursor, int size) {
    log.info("조회할 사용자 ID: {}", userId);

    boolean isKeywordSearch = keyword != null && !keyword.trim().isEmpty();
    Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id"));

    Page<MyCd> myCdsPage;

    try {
      if (isKeywordSearch) {
        myCdsPage = myCdRepository.searchMyCd(userId, keyword, cursor, size);
      } else {
        myCdsPage = (cursor == null || cursor == 0)
            ? myCdRepository.findByUserIdOrderByIdAsc(userId,
            PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id")))
            : myCdRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, cursor,
                PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id")));
      }
    } catch (Exception e) {
      throw new MyCdDatabaseException("CD 목록을 불러오는 중 오류가 발생했습니다.");
    }

    if (myCdsPage.isEmpty()) {
      throw new MyCdListEmptyException();
    }

    // 유저가 등록한 전체 CD 개수 조회
    long totalCount = myCdRepository.countByUserId(userId);

    // 유저가 등록한 전체 MyCd 중에서 가장 작은 ID와 가장 큰 ID 조회
    Long firstMyCdId = myCdRepository.findFirstByUserIdOrderByIdAsc(userId)
        .map(MyCd::getId)
        .orElseThrow(() -> new MyCdDatabaseException("사용자의 첫 번째 CD를 찾을 수 없습니다."));

    Long lastMyCdId = myCdRepository.findFirstByUserIdOrderByIdDesc(userId)
        .map(MyCd::getId)
        .orElseThrow(() -> new MyCdDatabaseException("사용자의 마지막 CD를 찾을 수 없습니다."));

    // 커서 기반 페이지네이션의 cursor 값 검증
    if (cursor != null && cursor > lastMyCdId) {
      throw new MyCdPaginationException("잘못된 커서 값입니다. (마지막 등록된 CD보다 큼)");
    }

    // 현재 페이지의 데이터 가져오기
    List<MyCd> myCds = myCdsPage.getContent();
    return MyCdListResponse.fromEntities(myCds, totalCount, firstMyCdId, lastMyCdId);

  }

  public MyCdResponse getMyCd(Long targetUserId, Long myCdId) {
    MyCd myCd = myCdRepository.findByIdAndUserId(myCdId, targetUserId)
        .orElseThrow(MyCdNotFoundException::new);

    return MyCdResponse.fromEntity(myCd);
  }

  public void delete(Long userId, List<Long> myCdIds) {
    // 삭제할 CD 목록을 가져오기
    List<MyCd> myCds = myCdRepository.findAllById(myCdIds);

    // 존재하지 않는 경우 예외 처리
    if (myCds.isEmpty()) {
      throw new MyCdNotFoundException();
    }

    // 로그인한 사용자가 소유한 CD인지 검증
    for (MyCd myCd : myCds) {
      if (!myCd.getUser().getId().equals(userId)) {
        throw new MyCdUnauthorizedException(); // 새 예외 적용
      }
    }

    // 실제 삭제 수행
    myCdRepository.deleteByUserIdAndIds(userId, myCdIds);
    //cd 삭제 후 이벤트 발행
    eventPublisher.publishEvent(new CdCollectionEvent.CdRemovedEvent(this, userId));
    log.debug("Published CD removed event for user: {}", userId);
  }

}