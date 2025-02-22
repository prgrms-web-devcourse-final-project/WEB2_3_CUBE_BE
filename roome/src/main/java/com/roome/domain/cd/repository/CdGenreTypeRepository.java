package com.roome.domain.cd.repository;

import com.roome.domain.cd.entity.CdGenreType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CdGenreTypeRepository extends JpaRepository<CdGenreType, Long> {
  Optional<CdGenreType> findByName(String name);  // ✅ 장르 이름으로 조회하는 메서드 추가
}
