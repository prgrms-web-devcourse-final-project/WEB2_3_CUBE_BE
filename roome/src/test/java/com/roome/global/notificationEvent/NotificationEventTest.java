//package com.roome.global.notificationEvent;
//
//import com.roome.domain.cdcomment.notificationEvent.CdCommentCreatedEvent;
//import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
//import com.roome.domain.houseMate.notificationEvent.HouseMateCreatedEvent;
//import com.roome.domain.notification.dto.NotificationType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class NotificationEventTest {
//
//    // NotificationEvent 클래스는 추상 클래스이므로 테스트하지 않음
//    // NotificationEvent 클래스의 하위 클래스인 CdCommentCreatedEvent, GuestBookCreatedEvent, HouseMateCreatedEvent 클래스를 테스트
//    // CdCommentCreatedEvent 클래스는 CdCommentCreatedEventTest 클래스에서 테스트
//    // GuestBookCreatedEvent 클래스는 GuestBookCreatedEventTest 클래스에서 테스트
//    // HouseMateCreatedEvent 클래스는 HouseMateCreatedEventTest 클래스에서 테스트
//
//    // CD 댓글 생성 이벤트 테스트
//    @Test
//    @DisplayName("CD 댓글 생성 이벤트가 올바른 정보를 포함하는지 검증한다")
//    void testCdCommentCreatedEvent() {
//        // Given
//        Object source = new Object();
//        Long senderId = 1L;
//        Long receiverId = 2L;
//        Long cdId = 3L;
//        Long commentId = 4L;
//        LocalDateTime createdAt = LocalDateTime.now();
//        // When
//        CdCommentCreatedEvent event = new CdCommentCreatedEvent(source, senderId, receiverId, cdId, commentId, createdAt);
//
//        // Then
//        assertThat(event.getSource()).isEqualTo(source);
//        assertThat(event.getSenderId()).isEqualTo(senderId);
//        assertThat(event.getReceiverId()).isEqualTo(receiverId);
//        assertThat(event.getCdId()).isEqualTo(cdId);
//        assertThat(event.getCommentId()).isEqualTo(commentId);
//        assertThat(event.getTargetId()).isEqualTo(cdId);
//        assertThat(event.getType()).isEqualTo(NotificationType.MUSIC_COMMENT);
//    }
//
//    // 방명록 생성 이벤트 테스트
//    @Test
//    @DisplayName("방명록 생성 이벤트가 올바른 정보를 포함하는지 검증한다")
//    void testGuestBookCreatedEvent() {
//        // Given
//        Object source = new Object();
//        Long senderId = 1L;
//        Long receiverId = 2L;
//        Long targetId = 3L;
//
//        // When
//        GuestBookCreatedEvent event = new GuestBookCreatedEvent(source, senderId, receiverId, targetId, LocalDateTime.now());
//
//        // Then
//        assertThat(event.getSource()).isEqualTo(source);
//        assertThat(event.getSenderId()).isEqualTo(senderId);
//        assertThat(event.getReceiverId()).isEqualTo(receiverId);
//        assertThat(event.getTargetId()).isEqualTo(targetId);
//        assertThat(event.getType()).isEqualTo(NotificationType.GUESTBOOK);
//    }
//
//    // 하우스메이트 생성 이벤트 테스트
//    @Test
//    @DisplayName("하우스메이트 생성 이벤트가 올바른 정보를 포함하는지 검증한다")
//    void testHouseMateCreatedEvent() {
//        // Given
//        Object source = new Object();
//        Long senderId = 1L;
//        Long receiverId = 2L;
//        Long targetId = 3L;
//        LocalDateTime createdAt = LocalDateTime.now();
//        // When
//        HouseMateCreatedEvent event = new HouseMateCreatedEvent(source, senderId, receiverId, targetId, createdAt);
//
//        // Then
//        assertThat(event.getSource()).isEqualTo(source);
//        assertThat(event.getSenderId()).isEqualTo(senderId);
//        assertThat(event.getReceiverId()).isEqualTo(receiverId);
//        assertThat(event.getTargetId()).isEqualTo(targetId);
//        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
//        assertThat(event.getType()).isEqualTo(NotificationType.HOUSE_MATE);
//    }
//}