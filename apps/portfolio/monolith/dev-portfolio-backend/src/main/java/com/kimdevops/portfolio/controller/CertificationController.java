package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.entity.Certification;
import com.kimdevops.portfolio.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CertificationController — 모놀리스 레거시 컨트롤러 [content-service로 이미 분리됨].
 *
 * <p><b>Strangler Fig 상태:</b>
 * 게이트웨이가 이 경로를 해당 신규 서비스로 라우팅하도록 전환됐다.
 * 이 컨트롤러는 게이트웨이를 우회한 직접 접근(8080 포트) 시에만 동작한다.
 * 신규 서비스가 안정화되면 이 파일은 삭제 대상이다.</p>
 *
 * @FROM  K-portfolio 원본 (Phase 0 흡수)
 * @PHASE Phase 3
 * @RISK  신규 서비스와 로직 동기화 없음 — 운영에서는 게이트웨이를 통한 신규 서비스만 사용할 것
 */
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @Autowired
    private CertificationService certificationService;

    /** 자격증 목록 조회 (공개) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Certification>>> getAll() {
        try {
            return ResponseEntity.ok(ApiResponse.success(certificationService.getAll()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /** 자격증 추가 (관리자) */
    @PostMapping
    public ResponseEntity<ApiResponse<Certification>> create(@RequestBody Certification request) {
        try {
            Certification saved = certificationService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /** 자격증 삭제 (관리자) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        try {
            certificationService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
