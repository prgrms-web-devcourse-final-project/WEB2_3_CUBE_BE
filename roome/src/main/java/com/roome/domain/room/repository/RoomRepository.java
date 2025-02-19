package com.roome.domain.room.repository;

import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomNoFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    default Room getById(Long id) {
        return findById(id)
                .orElseThrow(RoomNoFoundException::new);
    }

    Optional<Room> findByUserId(Long userId);
}
