package com.roome.domain.mybookreview.service.request;

import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.entity.MyBookReview;

public record MyBookReviewCreateRequest(
        String title,
        String quote,
        String takeaway,
        String motivate,
        String topic,
        String freeFormText,
        String coverColor
) {

    public MyBookReview toEntity() {
        return MyBookReview.builder()
                .title(title)
                .quote(quote)
                .takeaway(takeaway)
                .motivate(motivate)
                .topic(topic)
                .freeFormText(freeFormText)
                .coverColor(CoverColor.valueOf(coverColor))
                .build();
    }
}
