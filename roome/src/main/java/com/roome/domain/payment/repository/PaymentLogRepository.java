package com.roome.domain.payment.repository;

import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

  // 특정 사용자(userId)의 결제 기록 조회
  List<PaymentLog> findByUserId(Long userId);

  // 연결된 결제(payment)를 함께 조회하여 조회 시 N+1을 방지한다.
  // 기존 로그(payment=null) 호환을 위해 LEFT JOIN FETCH를 사용한다.
  @Query(value = "SELECT pl FROM PaymentLog pl LEFT JOIN FETCH pl.payment WHERE pl.user = :user",
      countQuery = "SELECT COUNT(pl) FROM PaymentLog pl WHERE pl.user = :user")
  Page<PaymentLog> findByUserWithPayment(@Param("user") User user, Pageable pageable);

  Page<PaymentLog> findByUser(User user, Pageable pageable);

  // 특정 결제 키(paymentKey)로 결제 내역 조회
  List<PaymentLog> findByPaymentKey(String paymentKey);

  // 특정 사용자(userId)의 결제 로그 삭제
  void deleteByUserId(Long userId);
}
