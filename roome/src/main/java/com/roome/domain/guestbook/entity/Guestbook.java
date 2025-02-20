package com.roome.domain.guestbook.entity;

import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "guestbook")
public class Guestbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestbookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String nickname;
    private String profileImage;

    @Column(nullable = false, length = 1000)
    private String message;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private RelationType relation;

    @Builder
    public Guestbook(Room room, User user, String nickname, String profileImage, String message, RelationType relation) {
        this.room = room;
        this.user = user;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.relation = relation;
    }

}
