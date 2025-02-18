package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCdCount;
import com.roome.domain.room.entity.Room;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyCdCountRepository extends JpaRepository<MyCdCount, Long> {

  Optional<MyCdCount> findByRoom(Room room);
}
