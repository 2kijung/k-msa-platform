package com.kmsa.blog.config;

import com.kmsa.blog.entity.BlogPost;
import com.kmsa.blog.repository.BlogPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * DataInitializer — blog-service 초기 포스트 데이터 적재.
 *
 * [무엇을] 서비스 최초 기동 시 포트폴리오용 블로그 포스트 3개를 등록한다.
 * [왜] "실제 운영 중인 서비스"임을 증명하는 컨텐츠. MSA 전환 과정에서 배운 것들을 정리.
 *       면접관이 /api/blog/posts 를 호출했을 때 빈 응답이 아닌 실제 포스트가 보여야 한다.
 * [어떻게] 이미 데이터가 있으면 건너뜀(멱등성) → 재기동 시 중복 적재 방지.
 * @PHASE:4
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initBlogPosts(BlogPostRepository repo) {
        return args -> {
            // [왜] 멱등성 보장 — 이미 데이터가 있으면 건너뜀
            if (repo.count() > 0) {
                log.info("[blog] 초기 데이터 이미 존재 — 건너뜀");
                return;
            }

            // [포스트 1] MSA 전환기: Strangler Fig 패턴 적용 경험
            BlogPost p1 = new BlogPost();
            p1.setTitle("MSA 전환기 — Strangler Fig 패턴을 실제로 적용해봤다");
            p1.setContent("""
                    ## 왜 Strangler Fig인가

                    모놀리스를 한 번에 MSA로 전환하는 것은 Big Bang 방식이다.
                    실패 리스크가 크고, 실제 운영 중인 서비스에 적용하기 어렵다.
                    Strangler Fig는 기존 서비스를 죽이지 않고 새 서비스가 감싸며 점진적으로 대체한다.

                    ## 적용 순서

                    1. Phase 0: K-portfolio 모놀리스를 그대로 흡수 (apps/portfolio/monolith/)
                    2. Phase 1: auth-service 분리 — 게이트웨이(Nginx)가 /auth/* 트래픽을 새 서비스로
                    3. Phase 2: contact + notification 분리 — REST 서비스 간 통신
                    4. Phase 3~5: content·blog·analytics 순차 분리

                    ## 배운 것

                    - Docker Compose `depends_on: condition: service_healthy` 로 기동 순서 제어
                    - PostgreSQL 스키마 분리로 'DB per service'의 현실적 절충
                    - Nginx auth_request 로 게이트웨이 단일 인증 관문 구현
                    """);
            p1.setCategory("MSA");
            p1.setTags("MSA,Strangler-Fig,Spring-Boot,Docker");
            p1.setStatus(BlogPost.Status.PUBLISHED);
            p1.setPlatform("BOTH");
            p1.setPublishedAt(LocalDateTime.now().minusDays(7));
            p1.setFeatured(true);
            p1.setDisplayOrder(1);

            // [포스트 2] Zipkin 분산 트레이싱 도입기
            BlogPost p2 = new BlogPost();
            p2.setTitle("Spring Boot 3.2 Zipkin 분산 트레이싱 설정 — RestTemplateBuilder 함정");
            p2.setContent("""
                    ## 문제 상황

                    contact-service → notification-service 호출이 Zipkin에서 별개 트레이스로 나왔다.
                    분명히 micrometer-tracing-bridge-brave를 추가했는데 traceId가 전파되지 않았다.

                    ## 원인

                    ```java
                    // 이렇게 하면 tracing interceptor가 주입되지 않는다
                    @Bean
                    public RestTemplate restTemplate() {
                        return new RestTemplate();  // ❌
                    }
                    ```

                    Spring Boot 3.x의 `ObservationRestTemplateCustomizer`는
                    `RestTemplateBuilder`를 통해 생성된 빈에만 자동 적용된다.

                    ## 해결

                    ```java
                    @Bean
                    public RestTemplate restTemplate(RestTemplateBuilder builder) {
                        return builder.build();  // ✅ tracing interceptor 자동 주입
                    }
                    ```

                    ## 결과

                    contact-service → notification-service 호출이 3 spans로 하나의 trace에 연결:
                    - [contact-service] http post /contacts (68ms)
                    - [contact-service] http post (13ms) ← outgoing
                    - [notification-service] http post /notify/send (2ms)
                    """);
            p2.setCategory("DevOps");
            p2.setTags("Zipkin,Micrometer-Tracing,Spring-Boot,MSA");
            p2.setStatus(BlogPost.Status.PUBLISHED);
            p2.setPlatform("BOTH");
            p2.setPublishedAt(LocalDateTime.now().minusDays(1));
            p2.setFeatured(true);
            p2.setDisplayOrder(2);

            // [포스트 3] k6 부하테스트 측정 경험
            BlogPost p3 = new BlogPost();
            p3.setTitle("k6 부하테스트로 MSA 게이트웨이 성능 실측 — 직접 vs 게이트웨이 경유");
            p3.setContent("""
                    ## 측정 환경

                    - auth-service 직접 호출 (port 8081): TPS 44.7, p95 744ms, 에러율 0%
                    - Nginx 게이트웨이 경유 (port 8090): TPS 42.9, p95 1.29s, 에러율 0%

                    ## 발견한 문제

                    처음에는 http://localhost 로 테스트했는데 k6가 TLS 에러를 냈다.
                    원인: 로컬에서 Caddy가 포트 80을 점유하고 있어서 308 redirect → HTTPS → TLS 실패.

                    ## 해결

                    1. 게이트웨이를 8090으로 이전
                    2. auth-service 직접 테스트용 스크립트 별도 작성 (auth-direct.js)

                    ## 결론

                    게이트웨이 오버헤드: +549ms (p95 기준), TPS는 1.8 감소.
                    허용 범위 내 — 게이트웨이가 제공하는 인증·라우팅·보안 기능을 감안하면 합리적.
                    """);
            p3.setCategory("DevOps");
            p3.setTags("k6,부하테스트,MSA,Nginx-Gateway");
            p3.setStatus(BlogPost.Status.PUBLISHED);
            p3.setPlatform("VELOG");
            p3.setPublishedAt(LocalDateTime.now().minusDays(3));
            p3.setFeatured(false);
            p3.setDisplayOrder(3);

            repo.save(p1);
            repo.save(p2);
            repo.save(p3);
            log.info("[blog] 초기 포스트 3개 적재 완료");
        };
    }
}
