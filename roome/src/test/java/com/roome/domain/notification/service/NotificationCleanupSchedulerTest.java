package com.roome.domain.notification.service;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationCleanupSchedulerTest {

    @Autowired
    private NotificationCleanupScheduler notificationCleanupScheduler;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("30일 이전 알림 삭제 스케줄러 테스트 - 실제 레포지토리 사용")
    void cleanupOldNotifications() {
        // 기존 알림 삭제
        notificationRepository.deleteAll();

        // 1. 직접 SQL을 사용하여 과거 날짜의 알림 생성
        LocalDateTime oldDate1 = LocalDateTime.now().minusDays(31);
        LocalDateTime oldDate2 = LocalDateTime.now().minusDays(35);

        // 2. 30일 이내 알림 생성 (유지되어야 함)
        LocalDateTime recentDate1 = LocalDateTime.now().minusDays(29);
        LocalDateTime recentDate2 = LocalDateTime.now().minusDays(15);

        // 직접 SQL 삽입으로 날짜 지정
        insertNotificationWithDate(NotificationType.EVENT, 1L, 1L, 2L, oldDate1);
        insertNotificationWithDate(NotificationType.GUESTBOOK, 2L, 2L, 3L, oldDate2);
        insertNotificationWithDate(NotificationType.HOUSE_MATE, 3L, 3L, 1L, recentDate1);
        insertNotificationWithDate(NotificationType.MUSIC_COMMENT, 4L, 4L, 2L, recentDate2);

        // 데이터 생성 확인
        int totalCount = countNotifications();
        assertEquals(4, totalCount, "초기 설정된 알림 개수가 4개여야 합니다");

        // 30일 이전 알림만 조회
        int oldNotificationsCount = countOldNotifications(LocalDateTime.now().minusDays(30));
        assertEquals(2, oldNotificationsCount, "30일 이전 알림은 2개여야 합니다");

        // when: 스케줄러 실행
        notificationCleanupScheduler.cleanupOldNotifications();

        // then: 30일 이전 알림만 삭제되었는지 확인
        int remainingCount = countNotifications();
        assertEquals(2, remainingCount, "30일 이내 알림 2개만 남아야 합니다");

        // 남은 알림들의 생성일 확인
        List<Map<String, Object>> remainingNotifications = jdbcTemplate.queryForList(
                "SELECT * FROM notifications");

        // 모든 남은 알림의 생성일이 30일 이내인지 검증
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        for (Map<String, Object> notification : remainingNotifications) {
            LocalDateTime createdAt = ((java.sql.Timestamp) notification.get("created_at")).toLocalDateTime();
            assertEquals(true, createdAt.isAfter(thirtyDaysAgo) || createdAt.isEqual(thirtyDaysAgo),
                    "남은 알림은 30일 이내에 생성된 것이어야 합니다");
        }
    }

    // 특정 날짜로 알림 생성을 위한 헬퍼 메소드
    private void insertNotificationWithDate(NotificationType type, Long senderId, Long targetId,
                                            Long receiverId, LocalDateTime createdAt) {
        // ISO 날짜 형식으로 변환
        String formattedDate = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        jdbcTemplate.update(
                "INSERT INTO notifications (type, sender_id, target_id, receiver_id, is_read, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                type.name(), senderId, targetId, receiverId, false, formattedDate, formattedDate
        );
    }

    // 전체 알림 수 조회
    private int countNotifications() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notifications", Integer.class);
    }

    // 특정 날짜 이전 알림 수 조회
    private int countOldNotifications(LocalDateTime threshold) {
        String formattedDate = threshold.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE created_at < ?",
                Integer.class,
                formattedDate
        );
    }
}