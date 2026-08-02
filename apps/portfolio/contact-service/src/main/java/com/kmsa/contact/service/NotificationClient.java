package com.kmsa.contact.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * NotificationClient — notification-service REST 클라이언트 + 서킷브레이커.
 *
 * @FROM  Phase 2 기본 구현
 * @HOW   Phase 6 — Resilience4j @CircuitBreaker 추가.
 *        notification-service 장애가 contact-service 본 흐름(문의 접수)을 막으면 안 된다.
 *        서킷브레이커: 10건 중 50% 실패 시 OPEN → 5초 대기 → HALF-OPEN.
 *        fallback: 서킷 OPEN 시 로그만 남기고 정상 반환(본 흐름 무중단).
 * @PHASE:6
 * @SCALE 회복탄력성 축: 단일 서비스 장애가 전체로 전파되지 않도록 격리.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    @Value("${notification.service.url:http://localhost:8083}")
    private String notificationUrl;

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 알림 전송. 서킷브레이커로 보호됨.
     * [왜] notification-service 장애 → 서킷 OPEN → sendFallback 즉시 실행 → 문의 접수는 계속.
     */
    @CircuitBreaker(name = "notification", fallbackMethod = "sendFallback")
    public void send(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(
            notificationUrl + "/notify/send",
            new HttpEntity<>(Map.of("message", message), headers),
            Void.class
        );
        log.info("[contact] 알림 전송 완료: {}", message.substring(0, Math.min(50, message.length())));
    }

    /**
     * fallback: 서킷 OPEN 또는 예외 발생 시 호출됨.
     * [왜] 알림 실패가 문의 접수를 막으면 안 됨(부가 기능은 핵심 흐름을 차단 불가).
     *      fallback은 조용히 실패 — 본 흐름(contact 저장)은 이미 완료된 상태.
     */
    public void sendFallback(String message, Throwable t) {
        log.warn("[contact] 알림 전송 실패(서킷브레이커 fallback): {}", t.getMessage());
    }
}
