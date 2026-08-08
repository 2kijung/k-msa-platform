package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.dto.VisitorDTO;
import com.kimdevops.portfolio.dto.VisitorStatsDTO;
import com.kimdevops.portfolio.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * VisitorController — 모놀리스 레거시 컨트롤러 [analytics-service로 이미 분리됨].
 *
 * <p><b>Strangler Fig 상태:</b>
 * 게이트웨이가 이 경로를 해당 신규 서비스로 라우팅하도록 전환됐다.
 * 이 컨트롤러는 게이트웨이를 우회한 직접 접근(8080 포트) 시에만 동작한다.
 * 신규 서비스가 안정화되면 이 파일은 삭제 대상이다.</p>
 *
 * @FROM  K-portfolio 원본 (Phase 0 흡수)
 * @PHASE Phase 5
 * @RISK  신규 서비스와 로직 동기화 없음 — 운영에서는 게이트웨이를 통한 신규 서비스만 사용할 것
 */
@RestController
@RequestMapping("/visitors")
public class VisitorController {
    @Autowired
    private VisitorService visitorService;

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorDTO>> recordVisit(@RequestBody Map<String, String> request) {
        try {
            VisitorDTO visitor = visitorService.recordVisit(
                    request.get("ipAddress"),
                    request.get("userAgent"),
                    request.get("referer"),
                    request.get("page"),
                    request.get("sessionId")
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(visitor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VisitorStatsDTO>> getStats(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        try {
            VisitorStatsDTO stats = visitorService.getStats(start, end);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
