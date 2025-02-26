package com.roome.domain.furniture.service;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FurnitureService {

    private final UserRepository userRepository;
    private final FurnitureRepository furnitureRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void upgradeBookshelf(Long loginUserId, Long roomId, int selectedLevel) {
        User user = userRepository.getById(loginUserId);
        user.validateRoomOwner(roomId);

        Furniture furniture = furnitureRepository.findByRoomId(roomId).stream()
                .filter(f -> f.getFurnitureType() == FurnitureType.BOOKSHELF)
                .findFirst()
                .orElseThrow();
        PointHistory pointHistory = PointHistory.builder()
                .user(user)
                .amount(furniture.getUpgradePrice())
                .reason(PointReason.getBookShelfUpgradeLevelReason(furniture.getLevel()))
                .build();

        furniture.upgradeLevel(selectedLevel);
        pointHistoryRepository.save(pointHistory);
    }
}
