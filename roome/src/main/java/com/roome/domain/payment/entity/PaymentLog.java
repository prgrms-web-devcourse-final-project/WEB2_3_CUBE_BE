package com.roome.domain.payment.entity;

import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_logs")
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 결제한 사용자

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    private int earnedPoints; // 적립된 포인트

    @Column(nullable = false, unique = true)
    private String paymentKey; // 토스 결제 키

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PaymentLog(User user, int amount, int earnedPoints, String paymentKey) {
        this.user = user;
        this.amount = amount;
        this.earnedPoints = earnedPoints;
        this.paymentKey = paymentKey;
        this.createdAt = LocalDateTime.now();
    }

}
