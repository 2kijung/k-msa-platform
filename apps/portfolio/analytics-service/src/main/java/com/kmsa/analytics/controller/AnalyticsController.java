package com.kmsa.analytics.controller;

import com.kmsa.analytics.entity.Visitor;
import com.kmsa.analytics.repository.VisitorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AnalyticsController — 방문자 기록 + 통계 API.
 *
 * 라우팅:
 *   공개  POST /api/analytics/visitors → 방문 기록 (프론트가 페이지 방문 시 호출)
 *   보호  GET  /api/analytics/stats    → 통계 조회 (관리자)
 * @PHASE:5
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final VisitorRepository visitorRepo;

    public AnalyticsController(VisitorRepository visitorRepo) {
        this.visitorRepo = visitorRepo;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "analytics-service is running"));
    }

    /** [공개] 방문 기록. 프론트엔드가 페이지 로드 시 호출. */
    @PostMapping("/visitors")
    public ResponseEntity<Void> record(@RequestBody Visitor visitor,
                                       @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                       @RequestHeader(value = "User-Agent", required = false) String ua) {
        if (visitor.getIpAddress() == null && xff != null) {
            visitor.setIpAddress(xff.split(",")[0].trim());
        }
        if (visitor.getUserAgent() == null && ua != null) {
            visitor.setUserAgent(ua);
        }
        visitor.setVisitedAt(LocalDateTime.now());
        visitorRepo.save(visitor);
        return ResponseEntity.ok().build();
    }

    /** [보호] 총 방문자 수. */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of("totalVisitors", visitorRepo.count()));
    }
}
