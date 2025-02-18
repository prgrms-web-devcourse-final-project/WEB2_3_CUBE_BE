package com.roome.domain.mycd.service;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.repository.CdRepository;
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
import jakarta.transaction.Transactional;
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
}
