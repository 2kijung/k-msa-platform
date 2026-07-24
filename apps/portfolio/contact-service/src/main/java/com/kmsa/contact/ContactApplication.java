package com.kmsa.contact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * contact-service 진입점 (독립 프로세스, 포트 8085).
 * [왜/MSA] auth-service와 별개의 main() → 독립 배포·기동.
 */
@SpringBootApplication
public class ContactApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactApplication.class, args);
    }

    /** [왜] notification-service를 REST로 호출할 클라이언트. 서비스 간 통신은 REST(결합도↓). */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
