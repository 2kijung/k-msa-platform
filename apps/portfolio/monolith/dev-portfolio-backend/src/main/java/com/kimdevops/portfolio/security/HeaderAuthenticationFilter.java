package com.kimdevops.portfolio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * ============================================================================
 * HeaderAuthenticationFilter — 게이트웨이 신뢰 헤더 기반 인증
 * ============================================================================
 * [무엇을] 게이트웨이가 주입해 준 X-User-Id / X-User-Role 헤더를 읽어 인증 정보를 세팅한다.
 *
 * [왜/MSA] ★ 이 파일이 "MSA 전환"의 핵심 변화다.
 *   - 전(모놀리스): monolith가 직접 JWT를 파싱·검증했다(JwtAuthenticationFilter + JwtUtil).
 *   - 후(MSA): 인증 검증은 게이트웨이 + auth-service가 "한 번" 수행하고,
 *              monolith는 게이트웨이가 넣어준 X-User-Id 헤더만 신뢰한다.
 *   즉 monolith는 더 이상 토큰을 해석하지 않는다 → 인증 책임이 auth-service로 넘어갔다.
 *   이것이 "인증은 한 곳에서, 신뢰는 헤더로 전파"라는 MSA 인증 모델이다.
 *
 * [보안 전제] 이 헤더는 반드시 게이트웨이만 주입해야 한다.
 *   외부 클라이언트가 X-User-Id를 위조해 보내면 안 되므로, monolith 포트(8080)는
 *   외부에 직접 노출하지 않고 게이트웨이 뒤에만 둔다(compose/K8s 내부망). 게이트웨이는
 *   외부에서 온 X-User-* 헤더를 신뢰하지 않고, verify 결과로 직접 다시 세팅한다.
 * ============================================================================
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role   = request.getHeader("X-User-Role");

        // [어떻게] 게이트웨이가 인증을 통과시킨 요청에만 X-User-Id가 들어있다.
        //          있으면 그 사용자로 인증 컨텍스트를 세팅하고, 없으면(공개 요청 등) 그냥 통과한다.
        if (StringUtils.hasText(userId)) {
            // [왜] role이 "ADMIN"이면 권한은 "ROLE_ADMIN" 형태여야 Spring Security가 인식한다.
            String authority = "ROLE_" + (StringUtils.hasText(role) ? role : "USER");
            var auth = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
