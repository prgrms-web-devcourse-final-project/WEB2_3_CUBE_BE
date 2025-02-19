package com.roome.domain.mybook.service.response;

import com.roome.domain.book.entity.BookGenre;
import com.roome.domain.book.entity.Genre;
import com.roome.domain.mybook.entity.MyBook;

import java.time.LocalDate;
import java.util.List;

public record MyBookResponse(
        Long id,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String imageUrl,
        List<String> genreNames,
        Long page
) {

    public static MyBookResponse from(MyBook myBook) {
        return new MyBookResponse(
                myBook.getId(),
                myBook.getBook().getTitle(),
                myBook.getBook().getAuthor(),
                myBook.getBook().getPublisher(),
                myBook.getBook().getPublishedDate(),
                myBook.getBook().getImageUrl(),
                myBook.getBook().getBookGenres().stream()
                        .map(BookGenre::getGenre)
                        .map(Genre::getName)
                        .toList(),
                myBook.getBook().getPage()
        );
    }
}
