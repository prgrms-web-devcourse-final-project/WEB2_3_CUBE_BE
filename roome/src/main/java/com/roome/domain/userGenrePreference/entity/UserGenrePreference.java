package com.roome.domain.userGenrePreference.entity;

import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_genre_preferences")
public class UserGenrePreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre_type", nullable = false, length = 10)
    private GenreType genreType;

    @Column(nullable = false, length = 50)
    private String genreName;

    public static UserGenrePreference create(User user, GenreType genreType, String genreName) {
        return UserGenrePreference.builder()
                .user(user)
                .genreType(genreType)
                .genreName(genreName)
                .build();
    }
}