package com.roome.domain.mybook.service.response;

import com.roome.domain.mybook.entity.MyBook;

import java.time.LocalDate;

public record MyBookResponse(
        Long id,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String imageUrl,
        String category,
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
                myBook.getBook().getCategory(),
                myBook.getBook().getPage()
        );
    }
}
