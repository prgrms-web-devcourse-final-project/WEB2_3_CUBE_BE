package com.roome.domain.user.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_music_genres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "genre"}))
public class UserMusicGenre {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

    public static UserMusicGenre create(Long userId, MusicGenre genre) {
        return UserMusicGenre.builder()
                             .userId(userId)
                             .genre(genre)
                             .build();
    }
}