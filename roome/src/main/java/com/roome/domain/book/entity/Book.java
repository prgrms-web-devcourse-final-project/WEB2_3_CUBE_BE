package com.roome.domain.book.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishedDate;

    private String imageUrl;

    private String category;

    private Long page;

    public static Book create(Long isbn, String title, String author, String publisher, LocalDate publishedDate, String imageUrl, String category, Long page) {
        Book book = new Book();
        book.isbn = isbn;
        book.title = title;
        book.author = author;
        book.publisher = publisher;
        book.publishedDate = publishedDate;
        book.imageUrl = imageUrl;
        book.category = category;
        book.page = page;
        return book;
    }
}
