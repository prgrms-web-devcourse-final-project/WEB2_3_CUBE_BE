package com.roome.domain.payment.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

        log.info("Toss API 요청 URL: {}", requestUrl);
        log.info("Toss API 요청 Headers: {}", headers);
        log.info("Toss API 요청 Body: {}", requestBody);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // ✅ RestTemplate을 사용하여 POST 요청 수행
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.POST, requestEntity, String.class
        );

        log.info("Toss API 응답 - Status: {}, Headers: {}, Body: {}",
                response.getStatusCode(), response.getHeaders(), response.getBody());


        log.info("Toss 결제 승인 응답 - Status: {}, Body: {}", response.getStatusCode(), response.getBody());

        return response;
    }



    //토스 페이먼츠 API를 사용하여 결제 검증
    public boolean verifyPayment(String paymentKey, String orderId, int amount) {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(TOSS_API_URL + "/" + paymentKey)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encodeSecretKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, requestEntity, String.class
            );

            log.info("!!!!!! 토스 응답: status={}, body={}", response.getStatusCode(), response.getBody());


            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                // ✅ status 값 안전하게 가져오기
                String status = jsonResponse.has("status") ? jsonResponse.get("status").asText() : null;

                // ✅ amount 값 안전하게 가져오기 (card.amount or totalAmount 확인)
                int responseAmount = 0;
                if (jsonResponse.has("totalAmount")) {
                    responseAmount = jsonResponse.get("totalAmount").asInt();
                } else if (jsonResponse.has("card") && jsonResponse.get("card").has("amount")) {
                    responseAmount = jsonResponse.get("card").get("amount").asInt();
                }

                // ✅ 값이 올바르게 가져와졌는지 확인
                if (status == null) {
                    log.error("결제 검증 실패: status 값이 응답에 없음.");
                    return false;
                }
                if (responseAmount == 0) {
                    log.error("결제 검증 실패: amount 값이 응답에 없음.");
                    return false;
                }

                // ✅ 결제 상태와 금액 비교 후 검증 성공 여부 결정
                if ("DONE".equals(status) && responseAmount == amount) {
                    log.info("결제 검증 성공: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
                    return true;
                } else {
                    log.warn("결제 검증 실패: 예상 금액={}, 응답 금액={}, 상태={}", amount, responseAmount, status);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("결제 검증 중 오류 발생: paymentKey={}, orderId={}", paymentKey, orderId, e);
            throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        return false;
    }

    // Toss 결제 취소 요청
    public boolean cancelPayment(String paymentKey, String cancelReason, Integer cancelAmount) {
        String requestUrl = TOSS_API_URL + "/" + paymentKey + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(encodeSecretKey());

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


    // Secret Key를 Base64 인코딩하여 반환
    private String encodeSecretKey() {
        log.info("현재 사용 중인 Toss Secret Key: {}", secretKey);

        if (secretKey == null || secretKey.isEmpty()) {
            throw new RuntimeException("Toss Secret Key가 설정되지 않았습니다.");
        }

        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

}
