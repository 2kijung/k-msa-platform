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
