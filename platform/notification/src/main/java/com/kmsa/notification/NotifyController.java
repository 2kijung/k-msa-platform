package com.kmsa.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NotifyController — 알림 요청 수신
 * [무엇을] POST /notify/send { "message": "..." } 를 받아 Telegram으로 전송.
 * [왜/MSA] contact 등 다른 서비스가 이 REST 엔드포인트만 호출하면 된다(알림 구현은 여기 캡슐화).
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {

    private final TelegramService telegramService;

    public NotifyController(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody Map<String, String> body) {
        telegramService.send(body.getOrDefault("message", ""));
        return ResponseEntity.ok().build();
    }

    /** 헬스체크 (K8s probe) */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("notification-service is running");
    }
}
