package com.roome.domain.payment.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.dto.TossPaymentInfo;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${toss.secret-key}")
    private String secretKey;

    public ResponseEntity<String> requestConfirm(PaymentVerifyDto verifyDto) {
        log.info("[Toss 결제 승인 요청] paymentKey={}, orderId={}, amount={}",
                verifyDto.getPaymentKey(), verifyDto.getOrderId(), verifyDto.getAmount());

        String requestUrl = "https://api.tosspayments.com/v1/payments/confirm";

        // 승인 요청 바디 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", verifyDto.getOrderId());
        requestBody.put("amount", verifyDto.getAmount());
        requestBody.put("paymentKey", verifyDto.getPaymentKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encodeSecretKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.POST, requestEntity, String.class
            );
            log.info("Toss 결제 승인 응답 - Status: {}", response.getStatusCode());
            return response;
        } catch (HttpStatusCodeException e) {
            // Toss는 실패 시 응답 body에 {code, message}를 담아 주니까 이걸 파싱해 도메인 에러로 매핑
            throw mapConfirmError(e);
        }
    }

    // Toss 승인 실패 응답의 error code를 도메인 에러로 매핑
    // 알려진 코드만 세분화하고, 나머지는 일반 실패로 두되 실제 code와 message를 로그로 남김
    private BusinessException mapConfirmError(HttpStatusCodeException e) {
        String code = null;
        String message = null;
        try {
            JsonNode body = objectMapper.readTree(e.getResponseBodyAsString());
            code = body.path("code").asText(null);
            message = body.path("message").asText(null);
        } catch (Exception parseError) {
            log.warn("Toss 오류 응답 파싱 실패: body={}", e.getResponseBodyAsString());
        }

        log.error("Toss 결제 승인 실패: httpStatus={}, code={}, message={}",
                e.getStatusCode(), code, message);

        ErrorCode mapped = switch (code == null ? "" : code) {
            // 이미 승인 처리된 결제 (재시도/중복 요청)
            case "ALREADY_PROCESSED_PAYMENT" -> ErrorCode.PAYMENT_ALREADY_PROCESSED;
            // 카드사/결제 수단 거절 (사용자 조치 필요)
            case "REJECT_CARD_COMPANY", "REJECT_ACCOUNT_PAYMENT", "INVALID_STOPPED_CARD",
                 "EXCEED_MAX_DAILY_PAYMENT_COUNT", "NOT_ENOUGH_BALANCE",
                 "INVALID_CARD_EXPIRATION", "EXCEED_MAX_PAYMENT_AMOUNT" -> ErrorCode.PAYMENT_REJECTED;
            // 그 외: 실제 code는 로그로 남기고 일반 실패로 처리
            default -> ErrorCode.PAYMENT_VERIFICATION_FAILED;
        };
        return new BusinessException(mapped);
    }



    // orderId로 Toss 결제 조회 (대사 배치용 — PENDING 결제는 paymentKey가 없어 orderId로 조회)
    // Toss에 결제 기록이 없으면(결제창까지 도달하지 못한 경우) Optional.empty() 반환
    public Optional<TossPaymentInfo> findPaymentByOrderId(String orderId) {
        String requestUrl = TOSS_API_URL + "/orders/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodeSecretKey());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            return Optional.of(TossPaymentInfo.builder()
                    .paymentKey(json.path("paymentKey").asText(null))
                    .status(json.path("status").asText(null))
                    // 금액이 없으면 -1로 두어 어떤 결제 금액과도 일치하지 않게 함
                    .totalAmount(json.path("totalAmount").asInt(-1))
                    .approvedAt(parseTossDateTime(json.path("approvedAt").asText(null)))
                    .build());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Toss 결제 조회 실패: orderId={}", orderId, e);
            throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
    }

    // Toss 결제 취소 요청
    public boolean cancelPayment(String paymentKey, String cancelReason, Integer cancelAmount) {
        String requestUrl = TOSS_API_URL + "/" + paymentKey + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encodeSecretKey());

        // 요청 바디 생성
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);
        if (cancelAmount != null) {
            body.put("cancelAmount", cancelAmount);
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.POST, requestEntity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("결제 취소 성공: paymentKey={}, cancelAmount={}", paymentKey, cancelAmount);
                return true;
            }
        } catch (Exception e) {
            log.error("결제 취소 실패: paymentKey={}, error={}", paymentKey, e.getMessage());
        }

        return false;
    }


    // Toss의 ISO-8601 오프셋 시각(예: 2024-02-13T12:17:57+09:00)을 LocalDateTime으로 변환
    // 값이 없거나 형식이 잘못되면 null을 반환해 호출부가 서버 시각으로 대체하도록 함
    public static LocalDateTime parseTossDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(isoDateTime).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Secret Key를 Base64 인코딩하여 반환 (시크릿키는 절대 로깅 x)
    private String encodeSecretKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new RuntimeException("Toss Secret Key가 설정되지 않았습니다.");
        }

        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

}
