package com.roome.domain.mybook.service.request;

import com.roome.domain.book.entity.Book;

import java.time.LocalDate;
import java.util.List;

public record MyBookCreateRequest(
        Long isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String imageUrl,
        List<String> genreNames,
        Long page
) {

    public Book toBookEntity() {
        return Book.builder()
                .isbn(isbn())
                .title(title())
                .author(author())
                .publisher(publisher())
                .publishedDate(publishedDate())
                .imageUrl(imageUrl())
                .page(page())
                .build();
    }
}
