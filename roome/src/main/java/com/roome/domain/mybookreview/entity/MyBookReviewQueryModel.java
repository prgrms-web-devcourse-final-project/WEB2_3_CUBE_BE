package com.roome.domain.mybookreview.entity;

import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
public class MyBookReviewQueryModel {

    Long id;
    String title;
    String quote;
    String takeaway;
    String motivate;
    String topic;
    String freeFormText;
    String coverColor;
    String writeDateTime;

    public static MyBookReviewQueryModel create(MyBookReview myBookReview) {
        MyBookReviewQueryModel myBookReviewQueryModel = new MyBookReviewQueryModel();
        myBookReviewQueryModel.id = myBookReview.getId();
        myBookReviewQueryModel.title = myBookReview.getTitle();
        myBookReviewQueryModel.quote = myBookReview.getQuote();
        myBookReviewQueryModel.takeaway = myBookReview.getTakeaway();
        myBookReviewQueryModel.motivate = myBookReview.getMotivate();
        myBookReviewQueryModel.topic = myBookReview.getTopic();
        myBookReviewQueryModel.freeFormText = myBookReview.getFreeFormText();
        myBookReviewQueryModel.coverColor = myBookReview.getCoverColor().name();
        myBookReviewQueryModel.writeDateTime = myBookReview.getWriteDateTime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h시 mm분", Locale.KOREAN));
        return myBookReviewQueryModel;
    }

    public void updateBy(MyBookReview myBookReview) {
        this.id = myBookReview.getId();
        this.title = myBookReview.getTitle();
        this.quote = myBookReview.getQuote();
        this.takeaway = myBookReview.getTakeaway();
        this.motivate = myBookReview.getMotivate();
        this.topic = myBookReview.getTopic();
        this.freeFormText = myBookReview.getFreeFormText();
        this.coverColor = myBookReview.getCoverColor().name();
        this.writeDateTime = myBookReview.getWriteDateTime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h시 mm분", Locale.KOREAN));
    }
}
