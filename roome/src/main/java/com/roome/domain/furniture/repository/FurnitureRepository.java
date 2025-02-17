package com.roome.domain.furniture.repository;

import com.roome.domain.furniture.entity.Furniture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FurnitureRepository extends JpaRepository<Furniture, Long> {
    List<Furniture> findByRoomId(Long roomId);
}
