package com.roome.domain.notification.entity;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;
    //보낸 사람의 user_id
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    //보낸 사람의 이름
    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_profile_image")
    private String senderProfileImage;
    //받는 사람의 target_id(음악, 이벤트, 게스트북 등의 id)
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    //받는 사람의 user_id
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private boolean read;

    @Builder
    public Notification(NotificationType type, String title, String content,
                        Long senderId, String senderName, String senderProfileImage,
                        Long targetId, Long receiverId) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfileImage = senderProfileImage;
        this.targetId = targetId;
        this.receiverId = receiverId;
        this.read = false;
    }

    public void markAsRead() {
        this.read = true;
    }
}