package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.entity.CdGenre;
import com.roome.domain.cd.entity.CdGenreType;
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
import com.roome.domain.mycd.exception.MyCdUnauthorizedException;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomNoFoundException;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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

  public MyCdResponse addCdToMyList(Long userId, MyCdCreateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    Room room = roomRepository.findByUserId(userId).orElseThrow(RoomNoFoundException::new);

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

    if (myCdRepository.existsByUserIdAndCdId(userId, cd.getId())) {
      throw new MyCdAlreadyExistsException();
    }

    MyCd myCd = myCdRepository.save(MyCd.create(user, room, cd));

    MyCdCount myCdCount = myCdCountRepository.findByRoom(room)
        .orElseGet(() -> myCdCountRepository.save(MyCdCount.init(room)));
    myCdCount.increment();

    return MyCdResponse.fromEntity(myCd);
  }

  public MyCdListResponse getMyCdList(Long userId, String keyword, Long cursor, int size) {
    log.info("조회할 사용자 ID: {}", userId);

    boolean isKeywordSearch = keyword != null && !keyword.trim().isEmpty();
    Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id"));

    Page<MyCd> myCdsPage;

    if (isKeywordSearch) {
      myCdsPage = myCdRepository.searchByUserIdAndKeyword(userId, keyword, pageable);
    } else {
      myCdsPage = (cursor == null || cursor == 0)
          ? myCdRepository.findByUserIdOrderByIdAsc(userId, pageable) // 첫 페이지
          : myCdRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, cursor, pageable);
    }

    if (myCdsPage.isEmpty()) {
      throw new MyCdListEmptyException();
    }

    // 첫 번째 myCdId와 마지막 myCdId 가져오기
    List<MyCd> myCds = myCdsPage.getContent();
    Long firstMyCdId = myCds.get(0).getId();
    Long lastMyCdId = myCds.get(myCds.size() - 1).getId();

    return MyCdListResponse.fromEntities(myCds, myCdsPage.getTotalElements(), firstMyCdId, lastMyCdId);
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
  }

}