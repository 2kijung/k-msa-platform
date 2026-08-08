package com.kmsa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AuthApplication — auth-service 독립 프로세스 진입점.
 *
 * <p><b>MSA에서의 역할:</b> k-msa-platform 전체 인증 흐름의 관문.<br>
 * 이 main()이 실행됨으로써 auth-service가 독립 JVM 프로세스(컨테이너 1개)로 기동된다.
 * 로그인(JWT 발급)과 토큰 검증(/auth/verify)이 이 프로세스 안에서만 처리된다.</p>
 *
 * <p><b>왜 이 위치에 있는가:</b> platform/ 하위에 위치하는 이유는 auth-service가
 * 특정 도메인 앱(portfolio·budget·blog)이 아니라 플랫폼 인프라 계층의 공유 서비스이기
 * 때문이다. apps/ 서비스들은 auth-service를 호출하지만, auth-service는 apps/를 모른다.</p>
 *
 * <ul>
 *   <li>{@code @PLAN} MSA 2원칙 "독립 프로세스" 실현. 이 서비스만 재시작해도 다른 서비스에 영향 없음.</li>
 *   <li>{@code @FROM} K-portfolio 모놀리스의 AuthController + SecurityConfig를 Strangler Fig 패턴으로 분리.</li>
 *   <li>{@code @HOW} {@code @SpringBootApplication} 한 줄이 컴포넌트 스캔 + 자동설정 + properties 로딩을 모두 활성화.</li>
 *   <li>{@code @SCALE} auth-service는 무상태(JWT)이므로 인스턴스를 수평 복제해도 인증 일관성이 깨지지 않음.</li>
 *   <li>{@code @RISK} JVM 기동 시 DataInitializer가 admin 계정을 시드한다. 운영 환경에서는 반드시 기본 계정 교체 필요.</li>
 * </ul>
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
