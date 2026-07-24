package com.kimdevops.portfolio.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 개발/테스트 환경용 메모리 기반 캐시
     * 프로덕션에서는 Redis를 사용하도록 변경 권장
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "projects",           // 모든 프로젝트 목록
                "featured-projects",  // 주요 프로젝트 목록
                "project",            // 개별 프로젝트
                "contacts",           // 모든 연락처
                "visitors",           // 방문자 통계
                "stats"               // 통계 데이터
        );
    }
}
