package com.roome.domain.user.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_book_genres")
public class UserBookGenre extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookGenre genre;

    public static UserBookGenre create(User user, BookGenre genre) {
        return UserBookGenre.builder()
                            .user(user)
                            .genre(genre)
                            .build();
    }
}