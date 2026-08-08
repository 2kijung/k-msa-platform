package com.kmsa.contact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * contact-service 진입점 — K-MSA 플랫폼의 Phase 2 서비스 (포트 8085).
 *
 * <p><b>MSA에서의 역할:</b>
 * 포트폴리오 사이트 방문자가 남긴 문의(이름·이메일·제목·본문)를 접수·저장하고,
 * notification-service에 알림을 위임하는 단일 도메인 서비스.
 * contacts 테이블(PostgreSQL contact 스키마)만 소유하며,
 * 다른 서비스는 이 데이터에 직접 접근할 수 없다.</p>
 *
 * <p><b>설계 배경:</b>
 * K-portfolio 모놀리스에서 Contact 엔티티와 ContactService를 그대로 이관.
 * 모놀리스에서는 알림 로직이 ContactService 안에 인라인으로 존재했으나,
 * MSA 전환 시 "문의 저장"과 "알림 발송"의 책임을 분리했다.
 * notification-service가 추후 슬랙·SMS·이메일 등 다채널로 확장되더라도
 * contact-service 코드는 변경되지 않는다.</p>
 *
 * @FROM  K-portfolio ContactController·ContactService 이관 (Phase 2)
 * @HOW   REST + JPA + Resilience4j 서킷브레이커로 notification-service와 느슨하게 결합
 * @SCALE 문의 접수 부하가 증가하면 contact-service 레플리카만 늘리면 된다
 *        (notification-service·auth-service는 무영향)
 * @RISK  notification-service 장애 시 문의 저장 자체가 롤백되어서는 안 된다.
 *        → {@link NotificationClient} 서킷브레이커 fallback으로 "저장은 성공, 알림만 건너뜀" 보장
 * @PHASE 2
 */
@SpringBootApplication
public class ContactApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContactApplication.class, args);
    }

    /**
     * notification-service를 호출할 {@link RestTemplate} Bean 등록.
     *
     * <p><b>왜 RestTemplateBuilder.build()를 쓰는가:</b>
     * Spring Boot가 {@code RestTemplateBuilder}에 {@code ObservationRestTemplateCustomizer}를
     * 자동으로 등록한다. 이 customizer가 outgoing HTTP 요청에 {@code traceparent} / {@code b3}
     * 헤더를 자동 주입하므로, contact-service → notification-service 구간에서
     * Zipkin 분산 트레이싱의 traceId가 끊기지 않고 전파된다.</p>
     *
     * <p>{@code new RestTemplate()}으로 직접 생성하면 이 customizer가 적용되지 않아
     * Zipkin 화면에서 contact span과 notification span이 별개 trace로 분리된다.</p>
     *
     * @HOW RestTemplateBuilder → ObservationRestTemplateCustomizer 자동 적용 → Zipkin traceId 전파
     * @PLAN Phase 6: Micrometer Tracing 검증 — Zipkin에서 contact→notification 3 spans 확인
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
