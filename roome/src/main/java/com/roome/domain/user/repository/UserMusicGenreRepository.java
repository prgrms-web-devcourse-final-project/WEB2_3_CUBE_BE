package com.roome.domain.user.repository;

import com.roome.domain.user.entity.UserMusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMusicGenreRepository extends JpaRepository<UserMusicGenre, Long> {
    List<UserMusicGenre> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    long countByUserId(Long userId);
}