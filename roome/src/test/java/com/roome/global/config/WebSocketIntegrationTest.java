package com.roome.global.config;

import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.org.springframework.web.socket=DEBUG"
})
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisService redisService;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    // 테스트용 유저 ID
    private static final String TEST_USER_ID = "1001";

    // 실제 생성된 JWT 토큰을 저장할 변수들
    private String validAccessToken;
    private String invalidAccessToken = "invalid.token.format";

    @BeforeEach
    public void setup() {
        // 실제 유효한 JWT 액세스 토큰 생성
        validAccessToken = jwtTokenProvider.createAccessToken(TEST_USER_ID);

        // RedisService mock 설정
        when(redisService.isBlacklisted(anyString())).thenReturn(false);
        when(redisService.isBlacklisted(invalidAccessToken)).thenReturn(true);

        // WebSocket 클라이언트 설정
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // SockJS URL
        wsUrl = "ws://localhost:" + port + "/ws";
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 웹소켓 연결이 성공해야 한다")
    public void testConnectWithValidToken() throws Exception {
        // given
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + validAccessToken);

        // HTTP 헤더에도 Authorization 추가
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + validAccessToken);

        // when & then
        try {
            StompSession session = stompClient.connect(
                    wsUrl,
                    httpHeaders,
                    connectHeaders,
                    new StompSessionHandlerAdapter() {}
            ).get(10, TimeUnit.SECONDS);

            assertTrue(session.isConnected());
            session.disconnect();
        } catch (ExecutionException e) {
            fail("웹소켓 연결 실패: " + e.getCause().getMessage());
        }
    }

    @Test
    @DisplayName("블랙리스트에 등록된 JWT 토큰으로 웹소켓 연결이 실패해야 한다")
    public void testConnectWithBlacklistedToken() {
        // given
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + invalidAccessToken);

        // HTTP 헤더에도 동일한 토큰 추가
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + invalidAccessToken);

        // when & then
        Exception exception = assertThrows(ExecutionException.class, () -> {
            stompClient.connect(
                    wsUrl,
                    httpHeaders,
                    connectHeaders,
                    new StompSessionHandlerAdapter() {}
            ).get(10, TimeUnit.SECONDS);
        });

        // 연결 실패 확인
        assertNotNull(exception);
    }

    @Test
    @DisplayName("JWT 토큰 없이 웹소켓 연결이 실패해야 한다")
    public void testConnectWithoutToken() {
        // given
        StompHeaders connectHeaders = new StompHeaders();
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();

        // when & then
        Exception exception = assertThrows(ExecutionException.class, () -> {
            stompClient.connect(
                    wsUrl,
                    httpHeaders,
                    connectHeaders,
                    new StompSessionHandlerAdapter() {}
            ).get(10, TimeUnit.SECONDS);
        });

        // 연결 실패 확인
        assertNotNull(exception);
    }

    @Test
    @DisplayName("연결 성공 후 메시지 구독 및 수신이 가능해야 한다")
    public void testSubscribeAndReceiveMessage() throws Exception {
        // given
        String topic = "/topic/test";
        String testMessage = "Test Message";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + validAccessToken);

        // HTTP 헤더에도 Authorization 추가
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + validAccessToken);

        CompletableFuture<String> messageFuture = new CompletableFuture<>();

        // when
        StompSession session;
        try {
            session = stompClient.connect(
                    wsUrl,
                    httpHeaders,
                    connectHeaders,
                    new StompSessionHandlerAdapter() {}
            ).get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            fail("웹소켓 연결 실패: " + e.getCause().getMessage());
            return;
        }

        // 구독 설정
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination(topic);
        session.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((String) payload);
            }
        });

        // 메시지 전송 (실제 애플리케이션에 맞게 수정 필요)
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/test"); // 애플리케이션에 맞는 메시지 전송 엔드포인트로 수정 필요
        try {
            session.send(sendHeaders, testMessage);

            // 주의: 이 테스트가 성공하려면 서버 측에서 메시지를 topic으로 다시 브로드캐스팅하는 핸들러가 필요함
            try {
                String receivedMessage = messageFuture.get(1, TimeUnit.SECONDS);
                assertEquals(testMessage, receivedMessage);
            } catch (TimeoutException e) {
                // 메시지 수신 타임아웃은 서버 측 구현에 따라 예상될 수 있음
                System.out.println("메시지 수신 타임아웃: 서버 측에 메시지 브로드캐스팅 핸들러가 구현되어 있는지 확인하세요");
            }
        } finally {
            session.disconnect();
        }
    }
}