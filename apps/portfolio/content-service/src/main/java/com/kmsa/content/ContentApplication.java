package com.kmsa.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * content-service 진입점.
 *
 * @FROM  K-portfolio PortfolioBackendApplication
 * @HOW   Boot 3.2 그대로 유지. 인증/Security 스타터 제거(게이트웨이가 처리).
 * @PHASE:3 Phase 3 — monolith에서 콘텐츠 도메인 분리
 * @SCALE 트래픽 확장: 이 서비스만 독립 스케일아웃 가능(auth/contact와 분리)
 */
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
