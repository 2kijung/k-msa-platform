package com.kmsa.content.controller;

import com.kmsa.content.entity.*;
import com.kmsa.content.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ContentController — 포트폴리오 콘텐츠 REST API.
 *
 * 라우팅 원칙(게이트웨이 기준):
 *   공개 GET  /api/content/**   → 인증 없이 통과 (조회)
 *   보호 POST/PUT/DELETE /api/content/admin/** → X-User-Id 헤더 필수 (관리자)
 *
 * @FROM  K-portfolio 각 도메인 컨트롤러(Project/Career/Skill/Cert/Profile) 통합
 * @HOW   인증 없음 — 게이트웨이가 X-User-Id 주입, 이 서비스는 헤더만 신뢰.
 * @PHASE:3
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 헬스체크 (K8s probe) */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "content-service is running"));
    }

    // ═══════════════ Project (공개 조회) ═══════════════

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> projects() {
        return ResponseEntity.ok(contentService.findAllProjects());
    }

    @GetMapping("/projects/featured")
    public ResponseEntity<List<Project>> featuredProjects() {
        return ResponseEntity.ok(contentService.findFeaturedProjects());
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> project(@PathVariable Long id) {
        return contentService.findProject(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════ Project (관리 — 게이트웨이 보호) ═══════════════

    @PostMapping("/admin/projects")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.ok(contentService.saveProject(project));
    }

    @PutMapping("/admin/projects/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return ResponseEntity.ok(contentService.saveProject(project));
    }

    @DeleteMapping("/admin/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        contentService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════ Career ═══════════════

    @GetMapping("/careers")
    public ResponseEntity<List<Career>> careers() {
        return ResponseEntity.ok(contentService.findAllCareers());
    }

    @PostMapping("/admin/careers")
    public ResponseEntity<Career> createCareer(@RequestBody Career career) {
        return ResponseEntity.ok(contentService.saveCareer(career));
    }

    @DeleteMapping("/admin/careers/{id}")
    public ResponseEntity<Void> deleteCareer(@PathVariable Long id) {
        contentService.deleteCareer(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════ Skill ═══════════════

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> skills() {
        return ResponseEntity.ok(contentService.findAllSkills());
    }

    @PostMapping("/admin/skills")
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        return ResponseEntity.ok(contentService.saveSkill(skill));
    }

    @DeleteMapping("/admin/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        contentService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════ Certification ═══════════════

    @GetMapping("/certifications")
    public ResponseEntity<List<Certification>> certifications() {
        return ResponseEntity.ok(contentService.findAllCertifications());
    }

    @PostMapping("/admin/certifications")
    public ResponseEntity<Certification> createCertification(@RequestBody Certification cert) {
        return ResponseEntity.ok(contentService.saveCertification(cert));
    }

    @DeleteMapping("/admin/certifications/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        contentService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════ Profile ═══════════════

    @GetMapping("/profile")
    public ResponseEntity<Profile> profile() {
        return contentService.findProfile()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/profile")
    public ResponseEntity<Profile> saveProfile(@RequestBody Profile profile) {
        return ResponseEntity.ok(contentService.saveProfile(profile));
    }
}
