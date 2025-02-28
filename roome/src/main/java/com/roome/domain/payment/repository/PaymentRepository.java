package com.roome.domain.payment.repository;

import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // paymentKey로 결제 정보 조회
    Optional<Payment> findByPaymentKey(String paymentKey);

    // orderId로 결제 정보 조회
    Optional<Payment> findByOrderId(String orderId);

    // 특정 사용자(userId)의 결제 내역 조회
    List<Payment> findByUserId(Long userId);

    // 특정 사용자(userId)의 성공한 결제 내역 조회
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
