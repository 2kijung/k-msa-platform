package com.kmsa.template.controller;

import com.kmsa.template.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TemplateController — REST API 골격.
 *
 * [라우팅 패턴]
 *   공개  GET  /my-domain/items     → 인증 없이 조회 (게이트웨이 auth_request 없음)
 *   보호  POST /my-domain/admin/**  → X-User-Id 헤더 필수 (게이트웨이가 auth_request 후 주입)
 *
 * [왜 @RequestMapping] 클래스 레벨 prefix → 게이트웨이 nginx.conf의 location 블록과 1:1 대응.
 *
 * @PHASE: N (이 서비스의 Phase 번호)
 */
@RestController
@RequestMapping("/my-domain")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    /**
     * 헬스 엔드포인트. Docker HEALTHCHECK + K8s livenessProbe 대상.
     * [왜] /actuator/health와 별개로 서비스별 의미 있는 상태를 반환.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "my-service is running"));
    }

    /**
     * 공개 목록 조회.
     * [왜] GET 조회는 인증 없이 허용 — 게이트웨이 nginx.conf에서 이 경로는 auth_request 없음.
     */
    @GetMapping("/items")
    public ResponseEntity<List<?>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * 보호 생성 엔드포인트.
     * [왜] X-User-Id 헤더는 게이트웨이가 auth 검증 후 주입. 이 서비스는 검증 없이 신뢰.
     *      "인증은 게이트웨이에서 한 번, 신뢰는 헤더로 전파" 패턴.
     */
    @PostMapping("/admin/items")
    public ResponseEntity<?> create(
            @RequestBody Object body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // X-User-Id가 없으면 게이트웨이를 우회한 직접 호출 — 운영 환경에서는 차단됨
        return ResponseEntity.ok(service.create(body));
    }
}
