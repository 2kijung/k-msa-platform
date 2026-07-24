package com.kmsa.auth.controller;

import com.kmsa.auth.dto.Dtos.*;
import com.kmsa.auth.security.JwtUtil;
import com.kmsa.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * AuthController — auth-service의 HTTP 진입점
 * ============================================================================
 * 세 개의 엔드포인트가 각각 MSA에서 다른 역할을 한다:
 *   POST /auth/login   → 로그인, JWT 발급        (사용자용, 공개)
 *   GET  /auth/verify  → 토큰 검증 위임          (★게이트웨이 전용, MSA 핵심)
 *   GET  /auth/health  → 생사 확인               (K8s probe용, 공개)
 * ============================================================================
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * [무엇을] 로그인 → JWT 발급.
     * [왜] MSA 전체 인증의 시작점. 여기서 받은 토큰을 클라이언트가 이후 요청에 실어 보낸다.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
        } catch (IllegalArgumentException e) {
            // [왜] 인증 실패는 401. 메시지는 일반화돼 있어 계정 존재 여부를 노출하지 않음.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * ★★★ MSA 인증의 심장 ★★★
     * [무엇을] 게이트웨이(Nginx auth_request)가 "이 요청의 토큰이 유효한가?"를 물으러 호출하는 곳.
     * [어떻게]
     *   1) 게이트웨이가 원요청의 Authorization 헤더를 그대로 이 verify로 전달한다.
     *   2) 토큰을 파싱·검증한다.
     *   3) 유효 → 200 OK + 응답 헤더에 X-User-Id / X-User-Role 를 실어 돌려준다.
     *   4) 무효 → 401. 게이트웨이는 401을 받으면 원요청을 차단(로그인 페이지로 보냄).
     * [왜/MSA] 이 한 곳에서만 JWT를 검증한다. 게이트웨이는 verify가 돌려준 X-User-Id를
     *          하위 서비스로 주입하고, 하위 서비스(monolith/content/contact...)는
     *          JWT를 다시 파싱하지 않고 그 헤더만 신뢰한다.
     *          → "인증은 한 번, 신뢰는 헤더로 전파" = MSA 단일 인증 지점.
     *          (하위 서비스가 각자 또 검증하면 중복·강결합 → 분산 모놀리스가 됨)
     * [원본 대비] K-portfolio에는 이 verify가 아예 없었다. 이게 있어야 진짜 MSA 인증이 성립한다.
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 토큰 없음 → 차단
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAndValidate(token);   // 서명·만료 검증 (실패 시 예외)
            // [핵심] 게이트웨이가 하위 서비스로 넘겨줄 신뢰 헤더를 응답에 실어준다.
            return ResponseEntity.ok()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 위조/만료 → 차단
        }
    }

    /**
     * [무엇을] 헬스체크.
     * [왜/MSA] K8s가 이 경로로 liveness/readiness를 확인한다. 죽으면 K8s가 자동 재시작(Self-Healing),
     *          준비 안 됐으면 트래픽을 안 보냄. "장애 자동 복구"의 기반.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("auth-service is running"));
    }
}
