package com.roome.domain.user.repository;

import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.UserBookGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBookGenreRepository extends JpaRepository<UserBookGenre, Long> {
    // 사용자의 책 장르 조회
    List<UserBookGenre> findByUserId(Long userId);

    // 사용자의 책 장르 삭제
    void deleteByUserId(Long userId);

    // 사용자의 책 장르 수 조회
    long countByUserId(Long userId);

    // 사용자의 책 장르 중복 조회
    boolean existsByUserIdAndGenre(Long userId, BookGenre genre);

    // 사용자의 책 장르 중복 삭제
    @Query("SELECT COUNT(DISTINCT ug.genre) FROM UserBookGenre ug WHERE ug.userId = :userId")
    int countDistinctGenresByUserId(@Param("userId") Long userId);
}