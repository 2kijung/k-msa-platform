package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.entity.Certification;
import com.kimdevops.portfolio.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
