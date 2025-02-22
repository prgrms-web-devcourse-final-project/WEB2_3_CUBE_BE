package com.roome.domain.mybookreview.entity;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybookreview.exception.MyBookReviewAuthorizationException;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    private LocalDateTime writeDateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_book_id")
    private MyBook myBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void update(MyBookReview myBookReview) {
        this.title = myBookReview.getTitle();
        this.quote = myBookReview.getQuote();
        this.takeaway = myBookReview.getTakeaway();
        this.motivate = myBookReview.getMotivate();
        this.topic = myBookReview.getTopic();
        this.freeFormText = myBookReview.getFreeFormText();
        this.coverColor = myBookReview.getCoverColor();
        this.writeDateTime = LocalDateTime.now();
    }

    public void validateOwner(Long userId) {
        if (user == null || !user.getId().equals(userId)) {
            throw new MyBookReviewAuthorizationException();
        }
    }
}
