package com.roome.domain.mybookreview.entity;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String quote;

    private String takeaway;

    private String motivate;

    private String topic;

    private String freeFormText;

    @Enumerated(EnumType.STRING)
    private CoverColor coverColor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_book_id")
    private MyBook myBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static MyBookReview create(String title, String quote, String takeaway, String motivate, String topic, String freeFormText, String coverColor, MyBook myBook, User user) {
        MyBookReview myBookReview = new MyBookReview();
        myBookReview.title = title;
        myBookReview.quote = quote;
        myBookReview.takeaway = takeaway;
        myBookReview.motivate = motivate;
        myBookReview.topic = topic;
        myBookReview.freeFormText = freeFormText;
        myBookReview.coverColor = CoverColor.valueOf(coverColor);
        myBookReview.myBook = myBook;
        myBookReview.user = user;
        return myBookReview;
    }

    public void update(String title, String quote, String takeaway, String motivate, String topic, String freeFormText, String coverColor) {
        this.title = title;
        this.quote = quote;
        this.takeaway = takeaway;
        this.motivate= motivate;
        this.topic = topic;
        this.freeFormText = freeFormText;
        this.coverColor = CoverColor.valueOf(coverColor);
    }

    public boolean isWrittenBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }
}
