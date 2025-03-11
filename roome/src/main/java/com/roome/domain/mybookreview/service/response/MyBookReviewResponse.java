package com.roome.domain.mybookreview.service.response;

import com.roome.domain.mybookreview.entity.MyBookReviewQueryModel;

import java.time.LocalDateTime;

public record MyBookReviewResponse(
        Long id,
        String title,
        String quote,
        String takeaway,
        String motivate,
        String topic,
        String freeFormText,
        String coverColor,
        LocalDateTime writeDateTime
) {

    public static MyBookReviewResponse from(MyBookReviewQueryModel myBookReviewQueryModel) {
        return new MyBookReviewResponse(
                myBookReviewQueryModel.getId(),
                myBookReviewQueryModel.getTitle(),
                myBookReviewQueryModel.getQuote(),
                myBookReviewQueryModel.getTakeaway(),
                myBookReviewQueryModel.getMotivate(),
                myBookReviewQueryModel.getTopic(),
                myBookReviewQueryModel.getFreeFormText(),
                myBookReviewQueryModel.getCoverColor(),
                myBookReviewQueryModel.getWriteDateTime()
        );
    }
}
