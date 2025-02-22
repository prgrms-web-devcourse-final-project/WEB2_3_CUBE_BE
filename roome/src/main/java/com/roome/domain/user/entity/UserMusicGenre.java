package com.roome.domain.user.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_music_genres")
public class UserMusicGenre {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

    public static UserMusicGenre create(User user, MusicGenre genre) {
        return UserMusicGenre.builder()
                             .user(user)
                             .genre(genre)
                             .build();
    }
}