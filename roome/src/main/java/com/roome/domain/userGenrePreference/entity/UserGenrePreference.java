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
@Table(name = "user_genre_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "genre_type", "rank"}))
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

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false)
    private Integer rank;

    public void updateCount(Integer count) {
        this.count = count;
    }

    public void updateRank(Integer rank) {
        this.rank = rank;
    }

    public static UserGenrePreference create(User user, GenreType genreType, String genreName, Integer count, Integer rank) {
        return UserGenrePreference.builder()
                .user(user)
                .genreType(genreType)
                .genreName(genreName)
                .count(count)
                .rank(rank)
                .build();
    }
}