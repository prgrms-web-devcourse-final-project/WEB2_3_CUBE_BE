package com.roome.domain.mybook.service.response;

import com.roome.domain.mybook.entity.MyBookQueryModel;

import java.util.List;

public record MyBooksResponse(
        List<MyBookResponse> myBooks,
        Long count
) {

    public static MyBooksResponse of(List<MyBookQueryModel> myBookQueryModels, Long count) {
        List<MyBookResponse> myBookResponses = myBookQueryModels.stream()
                .map(MyBookResponse::from)
                .toList();
        return new MyBooksResponse(
                myBookResponses,
                count
        );
    }
}
