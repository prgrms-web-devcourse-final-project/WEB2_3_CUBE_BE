package com.roome.domain.payment.service;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.Map;

public class TossPaymentService {
    private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";
    private static final String SECRET_KEY = "test_sk_vZnjEJeQVxGkg7xlkaAO3PmOoBN0";  // 운영 환경에서는 live_sk_YOUR_LIVE_KEY

    public Map<String, Object> requestPayment(String orderId, int amount) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 데이터 설정
        Map<String, Object> request = Map.of(
                "amount", amount,
                "orderId", orderId,
                "orderName", "테스트 결제",
                "successUrl", "http://localhost:8080/api/payments/verify",  // 백엔드 검증 API
                "failUrl", "http://localhost:8080/api/payments/fail/" + orderId  // 결제 실패 처리 API
        );

        // 인증 정보 (Basic Auth)
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(BASE_URL, HttpMethod.POST, entity, Map.class);

        return response.getBody();
    }
}
