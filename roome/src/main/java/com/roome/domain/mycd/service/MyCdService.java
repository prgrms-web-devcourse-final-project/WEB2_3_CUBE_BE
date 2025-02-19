package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.repository.CdRepository;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.mycd.exception.CdNotFoundException;
import com.roome.domain.mycd.exception.DuplicateCdException;
import com.roome.domain.room.exception.RoomNoFoundException;
import com.roome.domain.mycd.repository.MyCdCountRepository;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MyCdService {

  private final MyCdRepository myCdRepository;
  private final CdRepository cdRepository;
  private final MyCdCountRepository myCdCountRepository;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public MyCdResponse addCdToMyList(Long userId, Long cdId) {
    // 1. CD 존재 여부 확인
    Cd cd = cdRepository.findById(cdId)
        .orElseThrow(() -> new CdNotFoundException(cdId));

    // 2. 사용자 방 정보 가져오기
    Room room = roomRepository.findByUserId(userId)
        .orElseThrow(RoomNoFoundException::new);

    // 3. 이미 추가된 CD인지 확인
    if (myCdRepository.existsByUserIdAndCdId(userId, cdId)) {
      throw new DuplicateCdException(cdId);
    }

    // 4. MyCd 저장
    User user = room.getUser();  // Room에서 User 엔티티 가져오기
    MyCd myCd = myCdRepository.save(MyCd.create(user, room, cd));

    // 5. MyCdCount 증가
    MyCdCount myCdCount = myCdCountRepository.findByRoom(room)
        .orElseGet(() -> myCdCountRepository.save(MyCdCount.init(room)));
    myCdCount.increment();

    return MyCdResponse.fromEntity(myCd);
  }

  public MyCdListResponse getMyCdList(Long userId) {
    // 1. 사용자 존재 여부 확인
    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 2. 해당 사용자의 MyCd 목록 조회
    List<MyCdResponse> myCdResponses = myCdRepository.findByUserId(userId).stream()
        .map(MyCdResponse::fromEntity)
        .collect(Collectors.toList());

    return new MyCdListResponse(myCdResponses);
  }

  public MyCdResponse getMyCd(Long userId, Long myCdId) {
    // userId가 실제 존재하는지 검증
    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 해당 사용자의 특정 CD 조회
    MyCd myCd = myCdRepository.findByIdAndUserId(myCdId, userId)
        .orElseThrow(() -> new CdNotFoundException(myCdId));

    return MyCdResponse.fromEntity(myCd);
  }

}
