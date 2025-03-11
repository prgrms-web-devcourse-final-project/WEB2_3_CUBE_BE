package com.roome.domain.notification.listener;

import com.roome.domain.cdcomment.notificationEvent.CdCommentCreatedEvent;
import com.roome.domain.event.notificationEvent.EventUpcomingNotificationEvent;
import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
import com.roome.domain.houseMate.notificationEvent.HouseMateCreatedEvent;
import com.roome.domain.notification.dto.CreateNotificationRequest;
import com.roome.domain.notification.service.NotificationService;
import com.roome.domain.notification.service.NotificationWebSocketService;
import com.roome.domain.point.event.PointEvent;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.notificationEvent.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final NotificationWebSocketService notificationWebSocketService;

    /// 공통 알림 이벤트 처리 메서드
    /// 이벤트에서 필요한 정보를 추출하여 알림 생성 요청
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("알림 이벤트 수신: 유형={}, 발신자={}, 수신자={}, 대상ID={}",
                event.getType(), event.getSenderId(), event.getReceiverId(), event.getTargetId());

        try {
            // 이벤트 유효성 검증
            if (event.getType() == null || event.getSenderId() == null || event.getReceiverId() == null) {
                throw new BusinessException(ErrorCode.INVALID_NOTIFICATION_REQUEST);
            }

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .type(event.getType())
                    .senderId(event.getSenderId())
                    .receiverId(event.getReceiverId())
                    .targetId(event.getTargetId())
                    .build();

            Long notificationId = notificationService.createNotification(request);
            log.info("알림 생성 완료: 알림ID={}", notificationId);

            // 웹소켓을 통해 알림 전송
            try {
                notificationWebSocketService.sendNotificationToUser(
                        event.getReceiverId(),
                        notificationId,
                        event.getType()
                );
            } catch (BusinessException e) {
                // 웹소켓 전송 실패는 로그만 남기고 알림 처리 자체는 성공으로 처리
                log.error("웹소켓 알림 전송 실패: 에러코드={}, 메시지={}, 알림ID={}",
                        e.getErrorCode(), e.getMessage(), notificationId);
            }
        } catch (BusinessException e) {
            log.error("알림 생성 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            // 비즈니스 예외는 그대로 던져서 상위 계층에서 처리할 수 있도록 함
            throw e;
        } catch (Exception e) {
            log.error("알림 생성 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            // 알림 생성 실패를 명확한 비즈니스 예외로 변환
            throw new BusinessException(ErrorCode.NOTIFICATION_CREATION_FAILED);
        }
    }

    /// CD 코멘트 생성 이벤트 전용 핸들러
    /// 필요에 따라 CD 코멘트 관련 특별한 처리를 추가할 수 있음
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCdCommentCreated(CdCommentCreatedEvent event) {
        log.info("CD 코멘트 생성 이벤트 수신: 발신자={}, 수신자={}, CD ID={}, 코멘트 ID={}",
                event.getSenderId(), event.getReceiverId(), event.getCdId(), event.getCommentId());

        try {
            // CD 코멘트 특화 로직이 필요한 경우 여기에 추가
            // 예: 추가 정보 저장, 특정 조건에 따른 알림 필터링 등

            // 일반 알림 처리로 위임
            handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("CD 코멘트 알림 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    /// 방명록 생성 이벤트 전용 핸들러
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGuestBookCreated(GuestBookCreatedEvent event) {
        log.info("방명록 생성 이벤트 수신: 발신자={}, 수신자={}, 방명록 ID={}",
                event.getSenderId(), event.getReceiverId(), event.getTargetId());

        try {
            // 방명록 특화 로직이 필요한 경우 여기에 추가

            // 일반 알림 처리로 위임
            handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("방명록 알림 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    //하우스메이트 추가 이벤트 전용 핸들러
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleHouseMateCreated(HouseMateCreatedEvent event) {
        log.info("하우스메이트 추가 이벤트 수신: 발신자={}, 수신자={}, 대상 ID={}",
                event.getSenderId(), event.getReceiverId(), event.getTargetId());

        try {
            // 하우스메이트 특화 로직이 필요한 경우 여기에 추가

            // 일반 알림 처리로 위임
            handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("하우스메이트 알림 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    /// 이벤트 알림 전용 핸들러
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEventUpcoming(EventUpcomingNotificationEvent event) {
        log.info("이벤트 알림 수신: 발신자={}, 수신자={}, 이벤트 ID={}",
                event.getSenderId(), event.getReceiverId(), event.getTargetId());

        try {
            // 이벤트 특화 로직이 필요한 경우 여기에 추가
            // 이벤트 알림이므로 별도 처리 없이 일반 알림으로 처리

            // 일반 알림 처리로 위임
            handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("이벤트 알림 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    /// 포인트 적립 이벤트 핸들러
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePointEarned(PointEvent event) {
        log.info("포인트 적립 이벤트 수신: 발신자={}, 수신자={}, 포인트 ID={}, 금액={}, 사유={}",
                event.getSenderId(), event.getReceiverId(), event.getTargetId(),
                event.getAmount(), event.getReason());

        try {
            // 포인트 특화 로직이 필요한 경우 여기에 추가
            // 예: 특정 액수 이상의 포인트만 알림 보내기, 특정 사유에 따른 알림 메시지 차별화 등

            // 일반 알림 처리로 위임
            handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("포인트 적립 알림 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }
}