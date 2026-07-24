package com.kimdevops.portfolio.config;

import com.kimdevops.portfolio.security.HeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * ============================================================================
 * SecurityConfig (MSA 전환 후) — monolith는 인증을 "직접" 하지 않는다
 * ============================================================================
 * [무엇이 바뀌었나] auth 코드(AuthController/AuthService/JwtUtil/JwtAuthenticationFilter)를
 *   auth-service로 이관·삭제했다. 그래서:
 *     - PasswordEncoder 빈: 제거 (비밀번호 검증은 auth-service가 담당)
 *     - JWT 필터: 제거 → HeaderAuthenticationFilter(게이트웨이 헤더 신뢰)로 교체
 *     - /auth/** permitAll 규칙: 제거 (로그인 엔드포인트가 monolith에 없음. 게이트웨이가 auth-service로 보냄)
 * [왜/MSA] monolith는 이제 "게이트웨이가 인증한 결과(X-User-Id 헤더)"만 믿고 인가만 수행한다.
 *          인증 자체는 게이트웨이 + auth-service의 책임. → 책임 분리 = MSA.
 * ============================================================================
 */
@Configuration
public class SecurityConfig {

    // [변경] 기존 JwtAuthenticationFilter 대신, 게이트웨이 헤더를 신뢰하는 필터를 주입
    @Autowired
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    // [왜] 로그인 검증 자체는 auth-service로 이관됐지만, monolith의 DataInitializer가 아직
    //      사용자(admin) 시드에 BCrypt 인코더를 사용하므로 이 빈은 유지한다.
    //      @DEEP User 시드까지 auth-service로 완전 이관하면 이 빈과 User 엔티티도 제거.
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())                    // REST API라 CSRF 불필요
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // CORS preflight
                // 공개: 방문자기록/문의등록 (로그인은 이제 monolith에 없음 → 규칙에서 제외)
                .requestMatchers(HttpMethod.POST, "/contacts", "/visitors").permitAll()
                // 공개: 포트폴리오 조회(GET) + 업로드 이미지
                .requestMatchers(HttpMethod.GET, "/profile", "/careers", "/certifications",
                        "/dev-notes", "/projects", "/projects/**", "/skills", "/uploads/**").permitAll()
                // 공개: 문서/모니터링
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                        "/actuator/health", "/actuator/info", "/webjars/**").permitAll()
                // 그 외(추가/수정/삭제, 관리자 조회)는 인증 필요 → 게이트웨이가 넣어준 X-User-Id 있어야 통과
                .anyRequest().authenticated()
            )
            // [핵심] JWT 파싱 필터가 아니라, 게이트웨이 헤더 신뢰 필터를 체인에 삽입
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // [주의] 게이트웨이를 두면 CORS는 게이트웨이 단일 오리진에서 정리하는 게 이상적이나,
        //        전환 중이라 monolith 자체 CORS는 유지(추후 게이트웨이로 일원화 @DEEP).
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
