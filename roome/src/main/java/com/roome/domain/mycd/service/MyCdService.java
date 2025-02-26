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
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomNoFoundException;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    Room room = roomRepository.findByUserId(userId)
        .orElseThrow(RoomNoFoundException::new);

    Cd cd = cdRepository.findByTitleAndArtist(request.getTitle(), request.getArtist())
        .orElseGet(() -> {
          Cd newCd = Cd.create(
              request.getTitle(),
              request.getArtist(),
              request.getAlbum(),
              request.getReleaseDate(),
              request.getCoverUrl(),
              request.getYoutubeUrl(),
              request.getDuration()
          );

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

  public MyCdListResponse getMyCdList(Long userId, Long cursor, int size) {
    validateUser(userId);

    List<MyCd> myCds;
    if (cursor == null) {
      myCds = myCdRepository.findByUserIdOrderByIdAsc(userId, PageRequest.of(0, size));
    } else {
      myCds = myCdRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, cursor, PageRequest.of(0, size));
    }

    if (myCds.isEmpty()) {
      throw new MyCdListEmptyException();
    }

    return MyCdListResponse.fromEntities(myCds);
  }

  public MyCdResponse getMyCd(Long userId, Long myCdId) {
    validateUser(userId);

    MyCd myCd = myCdRepository.findByIdAndUserId(myCdId, userId)
        .orElseThrow(MyCdNotFoundException::new);

    return MyCdResponse.fromEntity(myCd);
  }

  public void delete(Long userId, List<Long> myCdIds) {
    validateUser(userId);

    if (myCdRepository.findAllById(myCdIds).isEmpty()) {
      throw new MyCdNotFoundException();
    }

    myCdRepository.deleteByUserIdAndIds(userId, myCdIds);
  }

  private void validateUser(Long userId) {
    userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
  }
}
