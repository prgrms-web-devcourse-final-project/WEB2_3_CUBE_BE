package com.roome.domain.payment.entity;

import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 결제한 사용자

    @Column(unique = true)
    private String paymentKey; // 결제 성공 시 반환되는 키

    @Column(nullable = false, unique = true)
    private String orderId; // 우리 서비스 내부 주문 ID

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    private int purchasedPoints; // 구매한 포인트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // 결제 상태

    private LocalDateTime approvedAt; // 결제 승인(완결) 시각 - 환불 기한 산정의 기준
    private LocalDateTime canceledAt; // 결제 취소 시각

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public void updatePaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    // 결제 완결(승인 확인) 처리 (승인 시각 기록)
    public void markApproved(String paymentKey, LocalDateTime approvedAt) {
        this.status = PaymentStatus.SUCCESS;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
    }

    // 결제 취소 처리(취소 시각 기록)
    public void markCanceled(LocalDateTime canceledAt) {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = canceledAt;
    }
}
