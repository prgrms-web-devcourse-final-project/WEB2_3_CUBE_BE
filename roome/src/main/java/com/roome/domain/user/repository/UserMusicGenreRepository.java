package com.roome.domain.user.repository;

import com.roome.domain.user.entity.MusicGenre;
import com.roome.domain.user.entity.UserMusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMusicGenreRepository extends JpaRepository<UserMusicGenre, Long> {
    // 유저별 음악 장르 조회
    List<UserMusicGenre> findByUserId(Long userId);
    // 유저별 음악 장르 삭제
    void deleteByUserId(Long userId);
    // 유저별 음악 장르 수 조회
    long countByUserId(Long userId);

    // 사용자의 음악 장르 중복 조회
    boolean existsByUserIdAndGenre(Long userId, MusicGenre genre);
    // 사용자의 책 장르 중복 삭제
    @Query("SELECT COUNT(DISTINCT ug.genre) FROM UserMusicGenre ug WHERE ug.userId = :userId")
    int countDistinctGenresByUserId(@Param("userId") Long userId);
}