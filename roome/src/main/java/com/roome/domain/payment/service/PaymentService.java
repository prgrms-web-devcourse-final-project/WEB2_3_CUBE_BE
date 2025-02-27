package com.roome.domain.payment.service;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final TossPaymentClient tossPaymentClient;

    // 사용자가 포인트 결제를 요청하면 DB에 저장해 줌
    // 결제 진행 중인 상태
    @Transactional
    public PaymentResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Payment payment = Payment.builder()
                .user(user)
                .orderId(requestDto.getOrderId())
                .amount(requestDto.getAmount())
                .purchasedPoints(requestDto.getPurchasedPoints())
                .status(PaymentStatus.PENDING)
                .paymentKey(null) // 결제 성공 후 업데이트 예정
                .build();

        paymentRepository.save(payment);
        log.info("결제 요청 저장 완료: orderId={}, amount={}, userId={}",
                payment.getOrderId(), payment.getAmount(), user.getId());

        return PaymentResponseDto.builder()
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .purchasedPoints(payment.getPurchasedPoints())
                .status(payment.getStatus())
                .build();
    }

    // 결제 성공 후, 토스 API의 응답을 검증하고 포인트 지급
    @Transactional
    public PaymentResponseDto verifyPayment(Long userId, PaymentVerifyDto verifyDto) {
        Payment payment = paymentRepository.findByOrderId(verifyDto.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 결제 금액 검증
        if (payment.getAmount() != verifyDto.getAmount()) {
            // !payment.getAmount().equals(verifyDto.getAmount()) 둘 중 뭐로 if문 쓸지 ?
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 토스 API에서 결제 상태 확인
        boolean isVerified = tossPaymentClient.verifyPayment(verifyDto.getPaymentKey(), verifyDto.getOrderId(), verifyDto.getAmount());
        if (!isVerified) {
            log.error("토스 결제 검증 실패: orderId={}, paymentKey={}", verifyDto.getOrderId(), verifyDto.getPaymentKey());
            throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        // 결제 상태 업데이트
        payment.updateStatus(PaymentStatus.SUCCESS);
        payment.updatePaymentKey(verifyDto.getPaymentKey());
        paymentRepository.save(payment);

        // 사용자 포인트 지급
        User user = payment.getUser();
        Point userPoint = pointRepository.findByUser(user)
                .orElseGet(() -> new Point(user, 0, 0, 0)); // 포인트 계정이 없으면 생성

        userPoint.addPoints(payment.getPurchasedPoints());
        pointRepository.save(userPoint);

        // 결제 내역 로그 저장
        PaymentLog paymentLog = PaymentLog.builder()
                .user(user)
                .amount(payment.getAmount())
                .earnedPoints(payment.getPurchasedPoints())
                .paymentKey(verifyDto.getPaymentKey())
                .build();
        paymentLogRepository.save(paymentLog);

        log.info("결제 성공 및 포인트 지급 완료: orderId={}, userId={}, pointsAdded={}",
                verifyDto.getOrderId(), user.getId(), payment.getPurchasedPoints());

        return PaymentResponseDto.builder()
                .orderId(payment.getOrderId())
                .paymentKey(verifyDto.getPaymentKey())
                .amount(payment.getAmount())
                .purchasedPoints(payment.getPurchasedPoints())
                .status(payment.getStatus())
                .build();
    }

    // 결제 실패 처리
    @Transactional
    public void failPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.updateStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.warn("결제 실패: orderId={}", orderId);
    }
}
