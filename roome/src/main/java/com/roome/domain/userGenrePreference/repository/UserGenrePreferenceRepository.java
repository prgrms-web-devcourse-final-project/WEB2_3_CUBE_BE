package com.roome.domain.userGenrePreference.repository;

import com.roome.domain.userGenrePreference.entity.GenreType;
import com.roome.domain.userGenrePreference.entity.UserGenrePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

    // 사용자 ID와 장르 타입으로 장르 선호도 조회 (rank 오름차순)
    List<UserGenrePreference> findByUserIdAndGenreTypeOrderByIdAsc(Long userId, GenreType genreType);
    // 사용자 ID와 장르 타입으로 모든 장르 선호도 삭제
    void deleteByUserIdAndGenreType(Long userId, GenreType genreType);
}
