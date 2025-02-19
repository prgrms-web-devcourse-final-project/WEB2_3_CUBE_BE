package com.roome.domain.mybook.service.response;

import com.roome.domain.mybook.entity.MyBook;

import java.util.List;

public record MyBooksResponse(
        List<MyBookResponse> myBooks,
        Long count
) {

    public static MyBooksResponse of(List<MyBook> myBooks, Long count) {
        List<MyBookResponse> myBookResponses = myBooks.stream()
                .map(MyBookResponse::from)
                .toList();
        return new MyBooksResponse(
                myBookResponses,
                count
        );
    }
}
