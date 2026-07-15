package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TossPaymentClientTest {

    @InjectMocks
    private TossPaymentClient tossPaymentClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private final String testSecretKey = "test_secret_key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(tossPaymentClient, "secretKey", testSecretKey);
    }

    @Test
    @DisplayName("Toss 승인 실패 - ALREADY_PROCESSED_PAYMENT는 PAYMENT_ALREADY_PROCESSED로 매핑된다.")
    void requestConfirm_AlreadyProcessed_Mapped() throws Exception {
        BusinessException thrown = assertConfirmErrorMapped(
                "{\"code\":\"ALREADY_PROCESSED_PAYMENT\",\"message\":\"이미 처리된 결제 입니다.\"}");
        assertEquals(ErrorCode.PAYMENT_ALREADY_PROCESSED, thrown.getErrorCode());
    }

    @Test
    @DisplayName("Toss 승인 실패 - 카드 거절 코드는 PAYMENT_REJECTED로 매핑된다.")
    void requestConfirm_CardRejected_Mapped() throws Exception {
        BusinessException thrown = assertConfirmErrorMapped(
                "{\"code\":\"REJECT_CARD_COMPANY\",\"message\":\"카드사에서 승인을 거절했습니다.\"}");
        assertEquals(ErrorCode.PAYMENT_REJECTED, thrown.getErrorCode());
    }

    @Test
    @DisplayName("Toss 승인 실패 - 알 수 없는 코드는 일반 실패(PAYMENT_VERIFICATION_FAILED)로 매핑된다.")
    void requestConfirm_UnknownCode_FallsBack() throws Exception {
        BusinessException thrown = assertConfirmErrorMapped(
                "{\"code\":\"SOME_NEW_CODE\",\"message\":\"알 수 없는 오류\"}");
        assertEquals(ErrorCode.PAYMENT_VERIFICATION_FAILED, thrown.getErrorCode());
    }

    // Toss가 4xx 오류 body를 반환하는 상황을 구성하고, requestConfirm이 던지는 BusinessException을 돌려준다.
    private BusinessException assertConfirmErrorMapped(String errorBody) throws Exception {
        HttpClientErrorException httpError = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                errorBody.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpError);
        when(objectMapper.readTree(errorBody)).thenReturn(new ObjectMapper().readTree(errorBody));

        return assertThrows(BusinessException.class,
                () -> tossPaymentClient.requestConfirm(new PaymentVerifyDto("pk123", "order123", 5000)));
    }

    @Test
    @DisplayName("orderId 결제 조회 성공 - 응답 파싱")
    void findPaymentByOrderId_Success() throws Exception {
        // given
        String responseBody = """
        {
            "paymentKey": "pk123",
            "status": "DONE",
            "totalAmount": 5000
        }
        """;
        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);
        when(objectMapper.readTree(responseBody)).thenReturn(new ObjectMapper().readTree(responseBody));

        // when
        var result = tossPaymentClient.findPaymentByOrderId("order123");

        // then
        assertTrue(result.isPresent());
        assertEquals("pk123", result.get().getPaymentKey());
        assertEquals("DONE", result.get().getStatus());
        assertEquals(5000, result.get().getTotalAmount());
    }

    @Test
    @DisplayName("orderId 결제 조회 - Toss에 기록이 없으면(404) 빈 Optional 반환")
    void findPaymentByOrderId_NotFound_ReturnsEmpty() {
        // given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));

        // when
        var result = tossPaymentClient.findPaymentByOrderId("order123");

        // then
        assertTrue(result.isEmpty());
    }
}
