package com.roome.domain.payment.entity;

import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 결제한 사용자

    @Column(nullable = false, unique = true)
    private String paymentKey; // 토스 결제 키

    @Column(nullable = false, unique = true)
    private String orderId; // 우리 서비스 내부 주문 ID

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    private int purchasedPoints; // 구매한 포인트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Payment(User user, String paymentKey, String orderId, int amount, int purchasedPoints, PaymentStatus status) {
        this.user = user;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.purchasedPoints = purchasedPoints;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
