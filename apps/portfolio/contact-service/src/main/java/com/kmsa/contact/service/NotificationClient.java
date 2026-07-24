package com.kmsa.contact.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ============================================================================
 * NotificationClient — notification-service REST 클라이언트
 * ============================================================================
 * [무엇을] 문의가 접수되면 notification-service(Telegram 등)에 알림 전송을 요청한다.
 * [왜/MSA] contact-service는 notification-service의 코드를 직접 import하지 않는다.
 *          오직 REST API로만 호출 → 서비스 간 결합도 0. notification이 재배포돼도 contact는 무관.
 *          (openclaw의 NotificationClient 패턴을 그대로 계승)
 * [★ 회복탄력성 원칙] 알림 전송 실패가 "문의 접수"라는 본 흐름을 절대 막으면 안 된다.
 *          그래서 모든 예외를 삼키고 로그만 남긴다(부가 기능은 핵심 흐름을 중단시키지 않는다).
 * [@DEEP] 이 동기 REST 호출을 Phase 6에서 이벤트(messaging) 발행으로 전환하면,
 *          contact는 "ContactCreated" 이벤트만 발행하고 notification이 구독 → 완전 비동기 분리.
 * ============================================================================
 */
@Component
public class NotificationClient {

    @Value("${notification.service.url:http://localhost:8083}")
    private String notificationUrl;

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** 알림 전송 요청. [왜] 실패해도 예외를 던지지 않는다 → 문의 접수는 항상 성공. */
    public void send(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                notificationUrl + "/notify/send",
                new HttpEntity<>(Map.of("message", message), headers),
                Void.class
            );
        } catch (Exception e) {
            // [핵심] 알림 실패가 문의 접수를 막으면 안 됨 → 삼키고 로그만.
            System.out.println("[contact-service] 알림 전송 실패(무시): " + e.getMessage());
        }
    }
}
