package com.roome.domain.mybookreview.service.response;

import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.entity.MyBookReview;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record MyBookReviewResponse(
        Long id,
        String title,
        String quote,
        String takeaway,
        String motivate,
        String topic,
        String freeFormText,
        CoverColor coverColor,
        String writeDateTime
) {

    public static MyBookReviewResponse from(MyBookReview myBookReview) {
        return new MyBookReviewResponse(
                myBookReview.getId(),
                myBookReview.getTitle(),
                myBookReview.getQuote(),
                myBookReview.getTakeaway(),
                myBookReview.getMotivate(),
                myBookReview.getTopic(),
                myBookReview.getFreeFormText(),
                myBookReview.getCoverColor(),
                myBookReview.getWriteDateTime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h시 mm분", Locale.KOREAN))
        );
    }
}
