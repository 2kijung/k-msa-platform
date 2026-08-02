package com.kmsa.blog.service;

import com.kmsa.blog.entity.BlogPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PostingClient — 외부 블로그 플랫폼 발행 클라이언트.
 *
 * @FROM  openclaw TistoryApiClient + VelogApiClient
 * @HOW   Spring Boot 3.2(jakarta)로 이관. 설정값 비어 있으면 dry-run 로그만.
 * @PHASE:4
 * @DEEP  실제 HTTP 구현: Tistory Open API (session 쿠키 기반), Velog GraphQL API
 */
@Component
public class PostingClient {

    private static final Logger log = LoggerFactory.getLogger(PostingClient.class);

    @Value("${blog.tistory.ts-session:}")
    private String tistorySession;

    @Value("${blog.velog.access-token:}")
    private String velogToken;

    /**
     * 플랫폼에 맞게 발행. 설정 없으면 dry-run.
     *
     * @return 발행된 외부 URL (dry-run 시 placeholder)
     * @DEEP 실제 Tistory/Velog API 호출 구현
     */
    public String publish(BlogPost post) {
        String platform = post.getPlatform();
        if (platform == null) platform = "BOTH";

        if (tistorySession.isEmpty() && velogToken.isEmpty()) {
            log.info("[blog] dry-run 발행: title={}, platform={}", post.getTitle(), platform);
            return "https://dry-run.example.com/" + post.getId();
        }

        // @DEEP 실제 API 호출:
        // if ("TISTORY".equals(platform) || "BOTH".equals(platform)) tistoryPost(post);
        // if ("VELOG".equals(platform) || "BOTH".equals(platform)) velogPost(post);
        throw new UnsupportedOperationException("@DEEP: Tistory/Velog API 구현 미완");
    }
}
