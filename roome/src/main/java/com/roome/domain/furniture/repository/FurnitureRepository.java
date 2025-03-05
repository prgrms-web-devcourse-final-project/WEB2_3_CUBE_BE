package com.roome.domain.furniture.repository;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.room.entity.Room;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FurnitureRepository extends JpaRepository<Furniture, Long> {

  List<Furniture> findByRoomId(Long roomId);

  Optional<Furniture> findByRoomAndFurnitureType(Room room, FurnitureType furnitureType);
}
