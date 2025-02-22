package com.roome.domain.user.repository;

import com.roome.domain.user.entity.UserBookGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBookGenreRepository extends JpaRepository<UserBookGenre, Long> {
    List<UserBookGenre> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    long countByUserId(Long userId);
}