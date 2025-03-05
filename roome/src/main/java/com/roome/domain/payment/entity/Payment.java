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
@Table(name = "payments")
public class Payment {

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

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public void updatePaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
}
