package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 파일 업로드 컨트롤러.
 * POST /api/files/upload (multipart) → ./uploads/ 에 저장 → 접근 URL 반환
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private final Path uploadDir = Paths.get("./uploads");

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("빈 파일입니다."));
            }
            Files.createDirectories(uploadDir);

            // 원본 확장자 유지 + 고유 파일명 생성
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Files.copy(file.getInputStream(), uploadDir.resolve(filename));

            // 상대경로 반환 → 어떤 도메인에서도 동작 (/api/uploads/파일명)
            String url = "/api/uploads/" + filename;

            return ResponseEntity.ok(ApiResponse.success(url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("업로드 실패: " + e.getMessage()));
        }
    }
}
