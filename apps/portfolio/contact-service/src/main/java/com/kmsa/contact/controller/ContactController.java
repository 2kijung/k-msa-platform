package com.kmsa.contact.controller;

import com.kmsa.contact.dto.ContactDtos.*;
import com.kmsa.contact.entity.Contact;
import com.kmsa.contact.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ContactController — 문의 API
 * [라우팅/MSA] 게이트웨이가 경로별로 공개/보호를 결정한다:
 *   POST /contacts        → 공개 (방문자가 문의 제출)
 *   GET  /admin/contacts  → 보호 (게이트웨이가 X-User-Id 검증 후 전달 → 관리자만)
 * contact-service 자체는 인증을 하지 않는다(게이트웨이가 이미 처리). 헤더가 있으면 신뢰.
 */
@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /** [공개] 문의 접수 → 저장 + 알림 */
    @PostMapping("/contacts")
    public ResponseEntity<ApiResponse<Long>> submit(@RequestBody ContactRequest req) {
        Contact saved = contactService.submit(req);
        return ResponseEntity.ok(ApiResponse.ok(saved.getId()));
    }

    /** [보호] 관리자 문의 목록 조회 (게이트웨이가 X-User-Id 주입한 요청만 도달) */
    @GetMapping("/admin/contacts")
    public ResponseEntity<ApiResponse<List<Contact>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(contactService.findAll()));
    }

    /** [공개] 헬스체크 (K8s probe) */
    @GetMapping("/contacts/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("contact-service is running"));
    }
}
