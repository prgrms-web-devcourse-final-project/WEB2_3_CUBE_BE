package com.roome.domain.room.entity;

import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "room_theme_unlock")
public class RoomThemeUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    private RoomTheme theme;

    public static RoomThemeUnlock create(User user, RoomTheme theme) {
        return RoomThemeUnlock.builder()
                .user(user)
                .theme(theme)
                .build();
    }
}
