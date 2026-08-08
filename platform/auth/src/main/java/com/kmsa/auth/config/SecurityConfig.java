package com.kmsa.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — auth-service 전용 Spring Security 설정.
 *
 * <p><b>MSA에서의 역할:</b> auth-service 자신의 엔드포인트에 대한 접근 규칙을 정의한다.
 * 단, 실제 트래픽 통제(인증 여부 판단)는 Nginx 게이트웨이가 {@code auth_request} 지시어로
 * {@code /auth/verify}를 호출하는 방식으로 처리된다. 이 SecurityConfig는 그 "검증 엔드포인트
 * 자체"가 외부 요청을 받을 수 있도록 열어두는 역할만 한다.</p>
 *
 * <p><b>왜 이 위치에 있는가:</b> auth-service는 자기 도메인(로그인·검증) 내의 보안만 책임진다.
 * 모놀리스였다면 URL 규칙이 수십 줄이었을 SecurityConfig가, MSA로 쪼개지니 단 2개 규칙으로 끝난다.
 * 이것이 MSA에서 보안 설정이 작고 명확해지는 이유다.</p>
 *
 * <ul>
 *   <li>{@code @PLAN} 모든 엔드포인트를 permitAll로 열되, 실제 인가 결정은 게이트웨이에 위임.</li>
 *   <li>{@code @FROM} K-portfolio SecurityConfig에서 "이 서비스에 해당하는 규칙"만 발췌·단순화.</li>
 *   <li>{@code @HOW} CSRF 비활성화(REST 무상태 API) + anyRequest().permitAll()(게이트웨이가 앞단 통제).</li>
 *   <li>{@code @SCALE} 무상태 구성이므로 인스턴스를 수평 확장해도 세션 공유 문제 없음.</li>
 *   <li>{@code @RISK} permitAll이 "인증 없이 누구나 접근 가능"을 의미하지 않는다.
 *       Nginx 게이트웨이가 내부 네트워크에서만 auth-service에 접근하도록 격리돼 있어야 이 설계가 안전하다.
 *       게이트웨이 없이 auth-service를 공개 인터넷에 직접 노출하면 위험.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * BCrypt 비밀번호 인코더 빈 등록.
     *
     * <p>{@code @PLAN} 평문 비밀번호 저장 금지. BCrypt는 salting이 내장돼 레인보우 테이블 공격에 안전하다.</p>
     * <p>{@code @HOW} AuthService의 {@code login()}에서 {@code passwordEncoder.matches(평문, 해시)}로
     * 검증하고, DataInitializer의 {@code seedAdmin()}에서 {@code passwordEncoder.encode(평문)}으로 저장한다.</p>
     * <p>{@code @SCALE} BCryptPasswordEncoder는 스레드 안전(thread-safe)하므로 싱글톤 빈으로 공유 가능.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 보안 필터 체인 설정.
     *
     * <p>{@code @PLAN} auth-service의 3개 엔드포인트(login·verify·health)를 모두 허용한다.
     * 각각의 이유:
     * <ul>
     *   <li>{@code /auth/login} — 로그인은 인증 전 단계이므로 당연히 열려야 함</li>
     *   <li>{@code /auth/verify} — 토큰 검증 자체가 인증 주체이므로 Spring Security가 또 막으면 순환 문제 발생</li>
     *   <li>{@code /auth/health} — K8s liveness/readiness probe는 인증 없이 도달해야 함</li>
     * </ul>
     * </p>
     * <p>{@code @HOW} CSRF 비활성화: REST API는 쿠키 기반 세션이 없으므로 CSRF 공격 벡터 자체가 없음.
     * sessionManagement를 별도 설정하지 않았지만 JWT 자체가 무상태이므로 세션이 생성되더라도 사용되지 않음.</p>
     * <p>{@code @RISK} 이 설정은 auth-service가 내부 네트워크(Docker 브리지 또는 K8s 클러스터 내부)에만
     * 존재한다는 전제에서 안전하다. 외부 노출 시 별도 IP 화이트리스트 또는 mTLS 추가 필요.</p>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // REST API + JWT 무상태 인증 → CSRF 토큰 불필요 (폼 세션 없음)
            .csrf(csrf -> csrf.disable())
            // 실제 접근 통제는 Nginx 게이트웨이가 담당하므로 여기서는 전 경로 허용
            .authorizeHttpRequests(authorizationConfig -> authorizationConfig
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
