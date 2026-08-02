package com.kmsa.blog.controller;

import com.kmsa.blog.entity.BlogPost;
import com.kmsa.blog.service.BlogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BlogController — 블로그 포스트 REST API.
 *
 * 라우팅:
 *   공개  GET  /api/blog/**       → 인증 없이 조회
 *   보호  POST/PUT/DELETE /api/blog/admin/** → X-User-Id 헤더 필수
 * @PHASE:4
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "blog-service is running"));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<BlogPost>> posts() {
        return ResponseEntity.ok(blogService.findPublished());
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogPost> post(@PathVariable Long id) {
        return blogService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/posts")
    public ResponseEntity<BlogPost> create(@RequestBody BlogPost post) {
        return ResponseEntity.ok(blogService.save(post));
    }

    @PutMapping("/admin/posts/{id}")
    public ResponseEntity<BlogPost> update(@PathVariable Long id, @RequestBody BlogPost post) {
        post.setId(id);
        return ResponseEntity.ok(blogService.save(post));
    }

    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
