package com.kmsa.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * analytics-service 진입점.
 * 방문자 기록·통계 전담. 읽기 부하 독립 스케일아웃 가능.
 *
 * @FROM  K-portfolio entity/Visitor·VisitorStats
 * @PHASE:5
 * @DEEP  CQRS: 쓰기(기록)와 읽기(집계) 분리 — 읽기에 Redis 캐시·DB 복제 적용 포인트
 */
@SpringBootApplication
public class AnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
