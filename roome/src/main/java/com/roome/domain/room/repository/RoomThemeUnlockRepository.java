package com.roome.domain.room.repository;

import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.entity.RoomThemeUnlock;
import com.roome.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomThemeUnlockRepository extends JpaRepository<RoomThemeUnlock, Long> {
    boolean existsByUserAndTheme(User user, RoomTheme theme);
    List<RoomThemeUnlock> findByUser(User user);
}
