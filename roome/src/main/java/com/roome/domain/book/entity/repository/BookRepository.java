package com.roome.domain.book.entity.repository;

import com.roome.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select distinct b from Book b left join fetch b.bookGenres bg left join fetch bg.genre where b.isbn = :isbn")
    Optional<Book> findByIsbn(String isbn);
}
