package com.kmsa.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================================
 * SecurityConfig — auth-service의 보안 설정
 * ============================================================================
 * [무엇을] BCrypt 인코더 등록 + 이 서비스의 엔드포인트 접근 규칙.
 * [왜/MSA] auth-service는 "게이트웨이 뒤"에 있고, 하는 일이 로그인/검증뿐이다.
 *          그래서 여기 엔드포인트(login/verify/health)는 전부 permitAll로 연다:
 *            - /auth/login  : 로그인은 당연히 인증 전에 열려 있어야 함
 *            - /auth/verify : 토큰 검증을 컨트롤러가 직접 하므로 시큐리티는 통과만
 *            - /auth/health : probe는 인증 없이 접근돼야 함
 *          진짜 접근 통제는 "게이트웨이 + verify" 조합이 담당한다(이 서비스 바깥).
 * [주의/원본 대비] K-portfolio의 SecurityConfig는 monolith라 모든 도메인 URL 규칙을
 *          한 곳에 몰아넣었다. auth-service는 자기 엔드포인트만 알면 되므로 훨씬 단순하다.
 *          → 서비스가 쪼개지니 각자의 보안 설정도 작고 명확해진다(MSA의 이점).
 * ============================================================================
 */
@Configuration
public class SecurityConfig {

    /** [무엇을] 비밀번호 해시 인코더. [왜] 평문 저장 금지 → BCrypt로 해시/검증. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // [왜] REST API + 무상태 토큰 인증이라 CSRF 토큰이 불필요(폼 세션이 아님)
            .csrf(csrf -> csrf.disable())
            // [왜] 이 서비스의 모든 엔드포인트는 게이트웨이 앞단에서 통제되므로 여기선 전부 허용.
            //      (verify 자체가 토큰을 검증하는 주체라 시큐리티로 또 막을 필요가 없음)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
