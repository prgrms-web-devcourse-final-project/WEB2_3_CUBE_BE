package com.roome.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishedDate;

    private String imageUrl;

    private Long page;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookGenre> bookGenres = new ArrayList<>();

    public void addBookGenre(BookGenre bookGenre) {
        bookGenres.add(bookGenre);
    }
}
