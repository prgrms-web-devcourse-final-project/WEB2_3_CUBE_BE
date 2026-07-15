package com.roome.domain.payment.entity;

import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payment_logs")
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 탈퇴 시에도 결제 이력은 보존하고 user 참조만 끊으므로 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 결제한 사용자 (탈퇴 후 비식별화되면 null)

    // 이 로그가 속한 결제 건 (로그를 Payment와 매칭하기 위한 연결)
    // (기존 로그 호환을 위해 nullable, 신규 로그는 항상 설정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    private int earnedPoints; // 적립된 포인트

    @Column(nullable = false)
    private String paymentKey; // 토스 결제 키

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isRefund = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 회원 탈퇴 시 개인 식별 참조만 끊어 결제 이력을 비식별 보존
    public void detachUser() {
        this.user = null;
    }
}
