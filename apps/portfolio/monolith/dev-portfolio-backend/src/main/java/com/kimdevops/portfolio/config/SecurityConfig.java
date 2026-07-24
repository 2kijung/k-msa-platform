package com.kimdevops.portfolio.config;

import com.kimdevops.portfolio.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인.
     * 이게 없으면 Spring Security 기본 보안이 켜져 모든 요청이 401로 차단된다.
     * (개발 단계) 모든 요청 허용 + CSRF 비활성 + H2 콘솔 허용.
     * JWT 강제 검증은 추후 JWT 필터로 추가 예정.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())                 // 위의 CORS 설정 사용
            .csrf(csrf -> csrf.disable())                    // REST API라 CSRF 불필요
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 콘솔 iframe 허용
            .authorizeHttpRequests(auth -> auth
                // CORS preflight 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 공개: 로그인 / 방문자기록 / 문의등록
                .requestMatchers(HttpMethod.POST, "/auth/**", "/contacts", "/visitors").permitAll()
                // 공개: 헬스체크 (K8s liveness/readiness probe)
                .requestMatchers(HttpMethod.GET, "/auth/health").permitAll()
                // 공개: 포트폴리오 조회(GET) + 업로드 이미지
                .requestMatchers(HttpMethod.GET, "/profile", "/careers", "/certifications",
                        "/dev-notes", "/projects", "/projects/**", "/skills", "/uploads/**").permitAll()
                // 공개: 문서/모니터링 (actuator는 health/info만 허용, h2-console은 dev only)
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                        "/actuator/health", "/actuator/info", "/webjars/**").permitAll()
                // 그 외(추가/수정/삭제, 관리자 조회)는 로그인 필요
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // allowCredentials(true)와 "*"는 setAllowedOrigins에 함께 못 쓴다.
        // → setAllowedOriginPatterns 를 쓰면 와일드카드 + 인증정보 허용이 가능하다. (개발 편의)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
