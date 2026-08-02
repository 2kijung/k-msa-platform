package com.kmsa.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * blog-service 진입점.
 *
 * @FROM  K-portfolio PortfolioBackendApplication(BlogPost·DevNote 부분)
 *         + openclaw blog-service(자동발행 엔진)
 * @HOW   Boot 3.2(jakarta). 자동발행: @EnableScheduling + TistoryApiClient·VelogApiClient.
 * @PHASE:4 Phase 4 — monolith에서 블로그 도메인 분리 + 자동발행 통합.
 * @SCALE 서비스 확장 축: 새 발행 채널(Velog·GitHub Pages 등) 추가 시 이 서비스만 수정.
 */
@SpringBootApplication
@EnableScheduling
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

    /** 외부 발행 API(Tistory·Velog) 호출용 */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
