package com.roome.domain.payment.repository;

import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

  // 특정 사용자(userId)의 결제 기록 조회
  List<PaymentLog> findByUserId(Long userId);

  Page<PaymentLog> findByUser(User user, Pageable pageable);

  // 특정 결제 키(paymentKey)로 결제 내역 조회
  List<PaymentLog> findByPaymentKey(String paymentKey);

  // 특정 사용자(userId)의 결제 로그 삭제
  void deleteByUserId(Long userId);
}
