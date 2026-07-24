package com.kmsa.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * TelegramService — 실제 Telegram 전송
 * [왜] 봇 토큰/챗ID가 설정돼 있을 때만 전송. 없으면 로그만 남기고 조용히 통과(개발/데모 안전).
 * [출처] openclaw notification-service의 TelegramNotificationService를 Boot 3.2로 이관.
 */
@Service
public class TelegramService {

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String message) {
        if (botToken.isBlank() || chatId.isBlank()) {
            System.out.println("[notification] Telegram 미설정 — 메시지 로그만: " + message);
            return;
        }
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            restTemplate.postForObject(url,
                Map.of("chat_id", chatId, "text", message, "parse_mode", "HTML"), Map.class);
        } catch (Exception e) {
            // [왜] 알림 실패가 호출자(contact)에 영향 주지 않도록 여기서도 예외를 삼킨다.
            System.out.println("[notification] Telegram 전송 실패: " + e.getMessage());
        }
    }
}
