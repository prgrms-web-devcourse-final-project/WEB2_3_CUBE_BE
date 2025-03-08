package com.roome.domain.mybook.service.response;

import com.roome.domain.mybook.entity.MyBookQueryModel;

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

    public static MyBookResponse from(MyBookQueryModel myBookQueryModel) {
        return new MyBookResponse(
                myBookQueryModel.getId(),
                myBookQueryModel.getTitle(),
                myBookQueryModel.getAuthor(),
                myBookQueryModel.getPublisher(),
                myBookQueryModel.getPublishedDate(),
                myBookQueryModel.getImageUrl(),
                myBookQueryModel.getGenreNames(),
                myBookQueryModel.getPage()
        );
    }
}
