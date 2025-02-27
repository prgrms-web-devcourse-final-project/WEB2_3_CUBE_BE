package com.roome.domain.payment.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String SECRET_KEY = "YOUR_TOSS_SECRET_KEY"; // 💡 토스 페이먼츠 Secret Key를 여기에 입력

    //토스 페이먼츠 API를 사용하여 결제 검증
    public boolean verifyPayment(String paymentKey, String orderId, int amount) {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(TOSS_API_URL + "/" + paymentKey)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(encodeSecretKey()); // Secret Key를 Base64 인코딩하여 인증

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, requestEntity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String status = jsonResponse.get("status").asText();
                int responseAmount = jsonResponse.get("amount").asInt();

                // 결제 상태와 금액이 일치하면 검증 성공
                if ("DONE".equals(status) && responseAmount == amount) {
                    log.info("✅ 결제 검증 성공: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
                    return true;
                } else {
                    log.warn("⚠️ 결제 검증 실패: 예상 금액={}, 응답 금액={}, 상태={}", amount, responseAmount, status);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("❌ 결제 검증 중 오류 발생: paymentKey={}, orderId={}", paymentKey, orderId, e);
            throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        return false;
    }

    // Secret Key를 Base64 인코딩하여 반환
    private String encodeSecretKey() {
        String key = SECRET_KEY + ":";
        return Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }
}
