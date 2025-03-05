package com.roome.domain.furniture.service;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.exception.BookshelfNotFound;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.furniture.exception.CdRackNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FurnitureService {

  private final PointService pointService;
  private final UserRepository userRepository;
  private final FurnitureRepository furnitureRepository;

  @Transactional
  public void upgradeBookshelf(Long loginUserId, Long roomId) {
    User user = userRepository.getById(loginUserId);
    user.validateRoomOwner(roomId);

    Furniture furniture = furnitureRepository.findByRoomId(roomId).stream()
        .filter(f -> f.getFurnitureType() == FurnitureType.BOOKSHELF)
        .findFirst()
        .orElseThrow(BookshelfNotFound::new);
    pointService.usePoints(user, PointReason.getBookShelfUpgradeReason(furniture.getLevel()));
    furniture.upgradeLevel();
  }

  @Transactional
  public void upgradeCdRack(Long loginUserId, Long roomId) {
    User user = userRepository.getById(loginUserId);
    user.validateRoomOwner(roomId);

    Furniture furniture = furnitureRepository.findByRoomId(roomId).stream()
        .filter(f -> f.getFurnitureType() == FurnitureType.CD_RACK)
        .findFirst()
        .orElseThrow(CdRackNotFoundException::new);

    // 업그레이드 시 필요한 포인트 사용
    pointService.usePoints(user, PointReason.getCdRackUpgradeReason(furniture.getLevel()));
    furniture.upgradeLevel();
  }

  public int getCdRackLevel(Room room) {
    return furnitureRepository.findByRoomAndFurnitureType(room, FurnitureType.CD_RACK)
        .map(Furniture::getLevel)
        .orElse(1); // 기본 레벨 1
  }

}
