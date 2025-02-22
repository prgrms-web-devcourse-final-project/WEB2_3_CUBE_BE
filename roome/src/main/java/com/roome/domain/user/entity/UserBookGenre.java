package com.roome.domain.user.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_book_genres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "genre"}))
public class UserBookGenre extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookGenre genre;

    public static UserBookGenre create(Long userId, BookGenre genre) {
        return UserBookGenre.builder()
                            .userId(userId)
                            .genre(genre)
                            .build();
    }
}