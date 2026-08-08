package com.kmsa.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * TemplateApplication — 새 서비스의 시작점 골격.
 *
 * [사용법] 이 파일을 복사해 클래스명·패키지명만 바꾼다:
 *   TemplateApplication → MyServiceApplication
 *   com.kmsa.template   → com.kmsa.myservice
 *
 * [왜/MSA] 각 서비스가 자기 main()을 가지는 것 자체가 "독립 배포 단위"의 증거.
 *          이 클래스가 있어야 `java -jar my-service.jar`로 독립 실행 가능.
 *
 * [왜 RestTemplateBuilder] 다른 서비스를 REST로 호출할 때 반드시 이 방식으로 주입.
 *   new RestTemplate()은 tracing interceptor가 빠져서 Zipkin traceId 전파 안 됨.
 *   RestTemplateBuilder.build()는 Spring Boot가 ObservationRestTemplateCustomizer 자동 적용.
 */
@SpringBootApplication
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }

    /**
     * [왜] 다른 서비스 REST 호출 시 이 Bean 주입받아 사용.
     *       tracing interceptor 자동 포함 → outgoing 요청에 traceparent 헤더 자동 추가.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
