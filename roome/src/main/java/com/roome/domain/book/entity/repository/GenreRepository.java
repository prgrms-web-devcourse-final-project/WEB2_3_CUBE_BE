package com.roome.domain.book.entity.repository;

import com.roome.domain.book.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    @Query("SELECT bg.genre.name FROM BookGenre bg WHERE bg.book.id IN " +
            "(SELECT mb.book.id FROM MyBook mb WHERE mb.room.id = :roomId)")
    List<String> findGenresByRoomId(Long roomId);
}
