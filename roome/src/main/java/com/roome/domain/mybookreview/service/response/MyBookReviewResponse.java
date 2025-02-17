package com.roome.domain.mybookreview.service.response;

import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.entity.MyBookReview;

public record MyBookReviewResponse(
        Long id,
        String title,
        String quote,
        String takeaway,
        String freeFormText,
        CoverColor coverColor
) {

    public static MyBookReviewResponse from(MyBookReview myBookReview) {
        return new MyBookReviewResponse(
                myBookReview.getId(),
                myBookReview.getTitle(),
                myBookReview.getQuote(),
                myBookReview.getTakeaway(),
                myBookReview.getFreeFormText(),
                myBookReview.getCoverColor()
        );
    }
}
