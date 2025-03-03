package com.roome.domain.cd.repository;

import com.roome.domain.cd.entity.CdGenreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CdGenreTypeRepository extends JpaRepository<CdGenreType, Long> {
  Optional<CdGenreType> findByName(String name);

  @Query("SELECT cg.genreType.name FROM CdGenre cg WHERE cg.cd.id IN " +
          "(SELECT mc.cd.id FROM MyCd mc WHERE mc.room.id = :roomId)")
  List<String> findGenresByRoomId(Long roomId);
}
