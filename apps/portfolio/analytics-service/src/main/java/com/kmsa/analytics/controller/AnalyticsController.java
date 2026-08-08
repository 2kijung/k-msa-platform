package com.kmsa.analytics.controller;

import com.kmsa.analytics.entity.Visitor;
import com.kmsa.analytics.repository.VisitorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AnalyticsController — 방문자 기록 + 기간별 통계 API.
 *
 * 라우팅:
 *   공개  POST /analytics/visitors    → 방문 기록 (프론트가 페이지 방문 시 호출)
 *   공개  GET  /analytics/stats       → 기간별 통계 (오늘/이번주/이번달/전체)
 *   보호  GET  /analytics/admin/stats → 상세 통계 (관리자 전용)
 *
 * [왜/MSA] 방문 기록과 통계 조회를 monolith에서 분리 → analytics 도메인이 독립 스케일아웃 가능.
 *          읽기 부하가 높을 때 analytics-service만 레플리카 증설(contact·auth에 영향 없음).
 * @PHASE:5
 * @SCALE analytics는 읽기 중심 → 추후 CQRS 기초(쓰기=여기, 읽기=replica) 적용 지점.
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

    /**
     * [공개] 방문 기록. 프론트엔드가 페이지 로드 시 호출.
     * [왜] X-Forwarded-For 헤더로 실제 클라이언트 IP를 추출 → 게이트웨이 뒤에서도 정확한 IP 기록.
     */
    @PostMapping("/visitors")
    public ResponseEntity<Void> record(
            @RequestBody Visitor visitor,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
            @RequestHeader(value = "User-Agent", required = false) String ua) {
        // [왜] 게이트웨이(Nginx)가 X-Forwarded-For를 설정 → 실제 클라이언트 IP (첫 번째 값)
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

    /**
     * [공개] 기간별 방문자 통계. 포트폴리오 메인 페이지에 "방문자 수" 표시용.
     * [왜] 공개 접근 가능 — 방문자 수는 포트폴리오 실제 운영 증거로 공개 가능한 수치.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart   = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart    = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthStart   = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // [왜] LinkedHashMap — 응답 JSON의 키 순서 보장(읽기 편의)
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("today",   visitorRepo.countByVisitedAtBetween(todayStart, now));
        result.put("thisWeek",  visitorRepo.countByVisitedAtBetween(weekStart, now));
        result.put("thisMonth", visitorRepo.countByVisitedAtBetween(monthStart, now));
        result.put("total",   visitorRepo.count());
        return ResponseEntity.ok(result);
    }

    /**
     * [보호] 상세 통계 — 관리자 전용. 게이트웨이가 auth_request로 검증.
     * [왜/MSA] 공개 통계와 관리자 통계를 경로로 분리 → 게이트웨이 auth_request 룰을 단순하게 유지.
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Long>> adminStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart   = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart    = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthStart   = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("today",     visitorRepo.countByVisitedAtBetween(todayStart, now));
        result.put("thisWeek",  visitorRepo.countByVisitedAtBetween(weekStart, now));
        result.put("thisMonth", visitorRepo.countByVisitedAtBetween(monthStart, now));
        result.put("total",     visitorRepo.count());
        return ResponseEntity.ok(result);
    }
}
