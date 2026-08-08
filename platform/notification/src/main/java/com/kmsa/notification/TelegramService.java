package com.kmsa.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * TelegramService — Telegram Bot API를 통한 실제 메시지 전송.
 *
 * <p><b>설계 원칙:</b>
 * 봇 토큰/챗ID가 환경변수로 주입된 경우에만 실제 전송하고,
 * 없으면 로그만 남기고 정상 반환(dry-run 모드).
 * 이렇게 하면 개발·데모 환경에서 실제 Telegram 계정 없이도 서비스가 기동되고
 * 전체 흐름(contact → notification HTTP 호출)을 테스트할 수 있다.</p>
 *
 * @FROM  openclaw-msa notification-service/TelegramNotificationService (Spring Framework 5.3 → Boot 3.2 이관)
 * @HOW   POST https://api.telegram.org/bot{token}/sendMessage → HTML parse_mode로 마크업 지원
 * @SCALE 채널 추가(Slack·SMS)는 이 클래스에 메서드 추가 or 별도 XxxService 주입. NotifyController는 변경 없음.
 * @RISK  Telegram API 장애 시 예외를 삼켜 호출자(contact-service)에 영향을 주지 않는다.
 *        단, 이러면 전송 실패가 무시될 수 있으므로 로그를 반드시 남긴다.
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 메시지를 Telegram으로 전송한다.
     *
     * @param message 전송할 텍스트 (HTML 마크업 허용)
     */
    public void send(String message) {
        // [왜] 설정값이 없으면 dry-run — 개발·데모 환경에서 안전하게 흐름만 확인
        if (botToken.isBlank() || chatId.isBlank()) {
            log.info("[notification] Telegram 미설정(dry-run) — 메시지: {}", message);
            return;
        }

        try {
            String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, String> payload = Map.of(
                "chat_id",    chatId,
                "text",       message,
                "parse_mode", "HTML"   // 마크업으로 <b>볼드</b> 등 서식 가능
            );
            restTemplate.postForObject(apiUrl, payload, Map.class);
            log.info("[notification] Telegram 전송 완료");
        } catch (Exception e) {
            // [왜] 알림 실패가 contact-service 응답(문의 저장 성공)에 영향을 주면 안 됨.
            //      예외를 삼키되 ERROR 로그를 남겨 운영자가 인지할 수 있게 한다.
            log.error("[notification] Telegram 전송 실패: {}", e.getMessage());
        }
    }
}
