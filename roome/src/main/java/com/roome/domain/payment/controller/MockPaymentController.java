package com.roome.domain.payment.controller;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 프론트 개발 협업용 Mock API - 운영(prod) 환경에서는 빈 자체가 등록되지 않는다.
// 새 프로파일이 추가되어도 기본적으로 제외되도록 화이트리스트 방식을 사용한다.
@Profile({"local", "dev", "test"})
@Hidden
@Slf4j
@RestController
@RequestMapping("/mock/payments")
@RequiredArgsConstructor
public class MockPaymentController {

  private static final int MAX_MOCK_ENTRIES = 1_000;

  // 무인증 엔드포인트이므로 동시 접근에 안전해야 하고, 크기를 제한해 메모리 고갈을 막는다
  private final Map<String, PaymentResponseDto> mockPaymentStorage =
      Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, PaymentResponseDto> eldest) {
          return size() > MAX_MOCK_ENTRIES;
        }
      });

  @PostMapping("/request")
  public ResponseEntity<PaymentResponseDto> mockRequestPayment(
      @RequestBody PaymentRequestDto requestDto) {

    log.info("[MOCK] 결제 요청: orderId={}, amount={}", requestDto.getOrderId(),
        requestDto.getAmount());

    PaymentResponseDto response = PaymentResponseDto.builder()
        .orderId(requestDto.getOrderId())
        .amount(requestDto.getAmount())
        .purchasedPoints(requestDto.getPurchasedPoints())
        .status(PaymentStatus.PENDING)
        .build();

    mockPaymentStorage.put(requestDto.getOrderId(), response);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/verify")
  public ResponseEntity<PaymentResponseDto> mockVerifyPayment(
      @RequestBody PaymentVerifyDto verifyDto) {

    log.info("🛠 [MOCK] 결제 검증 요청: orderId={}, paymentKey={}", verifyDto.getOrderId(),
        verifyDto.getPaymentKey());

    if (!mockPaymentStorage.containsKey(verifyDto.getOrderId())) {
      log.warn("⚠️ [MOCK] 결제 검증 실패: 주문 ID 없음 orderId={}", verifyDto.getOrderId());
      return ResponseEntity.badRequest().build();
    }

    PaymentResponseDto response = mockPaymentStorage.get(verifyDto.getOrderId());
    PaymentResponseDto updatedResponse = PaymentResponseDto.builder()
        .orderId(response.getOrderId())
        .paymentKey(verifyDto.getPaymentKey())
        .amount(response.getAmount())
        .purchasedPoints(response.getPurchasedPoints())
        .status(PaymentStatus.SUCCESS)
        .build();

    mockPaymentStorage.put(verifyDto.getOrderId(), updatedResponse);
    return ResponseEntity.ok(updatedResponse);
  }

  @PostMapping("/fail")
  public ResponseEntity<Void> mockFailPayment(@RequestParam String orderId) {

    log.warn("🛠 [MOCK] 결제 실패 처리: orderId={}", orderId);

    if (!mockPaymentStorage.containsKey(orderId)) {
      return ResponseEntity.badRequest().build();
    }

    PaymentResponseDto response = mockPaymentStorage.get(orderId);
    PaymentResponseDto failedResponse = PaymentResponseDto.builder()
        .orderId(response.getOrderId())
        .amount(response.getAmount())
        .purchasedPoints(response.getPurchasedPoints())
        .status(PaymentStatus.FAILED)
        .build();

    mockPaymentStorage.put(orderId, failedResponse);
    return ResponseEntity.noContent().build();
  }
}
