package com.roome.domain.furniture.service;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.exception.BookshelfNotFound;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
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
}
