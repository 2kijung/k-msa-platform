package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.dto.ProjectDTO;
import com.kimdevops.portfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * ProjectController — 모놀리스 레거시 컨트롤러 [content-service로 이미 분리됨].
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
@RequestMapping("/projects")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(@RequestBody Map<String, String> request) {
        try {
            ProjectDTO project = projectService.createProject(
                    request.get("title"),
                    request.get("description"),
                    request.get("technologies"),
                    request.getOrDefault("status", "DEVELOPMENT")
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(project));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ProjectDTO> projects = projectService.getProjects(page, size);
            return ResponseEntity.ok(ApiResponse.success(projects));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getFeaturedProjects() {
        try {
            List<ProjectDTO> projects = projectService.getProductionProjects();
            return ResponseEntity.ok(ApiResponse.success(projects));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            ProjectDTO project = projectService.updateProject(
                    id,
                    request.get("title"),
                    request.get("description"),
                    request.getOrDefault("status", "DEVELOPMENT")
            );
            return ResponseEntity.ok(ApiResponse.success(project));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
