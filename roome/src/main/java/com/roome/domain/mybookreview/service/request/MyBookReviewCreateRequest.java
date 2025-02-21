package com.roome.domain.mybookreview.service.request;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.user.entity.User;

public record MyBookReviewCreateRequest(
        String title,
        String quote,
        String takeaway,
        String motivate,
        String topic,
        String freeFormText,
        String coverColor
) {

    public MyBookReview toEntity(MyBook myBook, User user) {
        return MyBookReview.builder()
                .title(title)
                .quote(quote)
                .takeaway(takeaway)
                .motivate(motivate)
                .topic(topic)
                .freeFormText(freeFormText)
                .coverColor(CoverColor.valueOf(coverColor))
                .myBook(myBook)
                .user(user)
                .build();
    }
}
