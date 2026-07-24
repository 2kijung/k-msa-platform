package com.kmsa.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/** notification-service 진입점 (포트 8083, 무상태 알림 서비스). */
@SpringBootApplication
public class NotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

    /** [왜] Telegram API 호출용 RestTemplate. */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
