package com.kmsa.blog.service;

import com.kmsa.blog.entity.BlogPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * PostingClient — 외부 블로그 플랫폼 발행 클라이언트 (Tistory · Velog).
 *
 * @FROM  openclaw-msa blog-service/TistoryApiClient + VelogApiClient
 *        (Spring Framework 5.3, javax → jakarta로 이관)
 * @HOW   설정값(tistorySession / velogToken)이 비어 있으면 dry-run(로그만).
 *        설정 주입: docker-compose의 TISTORY_TSSESSION, VELOG_ACCESS_TOKEN 환경변수.
 * @SCALE 새 플랫폼(GitHub Pages, Medium) 추가 시 이 클래스에만 메서드 추가.
 *        blog-service 전용 → 다른 서비스 배포에 영향 없이 채널 확장 가능.
 * @PHASE:4
 */
@Component
public class PostingClient {

    private static final Logger log = LoggerFactory.getLogger(PostingClient.class);

    @Value("${blog.tistory.ts-session:}")
    private String tistorySession;

    @Value("${blog.tistory.blog-name:dlrlwjd1313}")
    private String tistoryBlogName;

    @Value("${blog.velog.access-token:}")
    private String velogToken;

    @Value("${blog.velog.username:dlrlwjd1313}")
    private String velogUsername;

    private final RestTemplate restTemplate;

    public PostingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 플랫폼에 맞게 외부 발행. 설정 없으면 dry-run.
     *
     * [왜] 설정이 없어도 서비스는 정상 기동 → 데모 환경에서 Telegram/Tistory 토큰 없이도 흐름 확인 가능.
     * [어떻게] platform 값에 따라 Tistory·Velog 중 하나 또는 둘 다 발행.
     *
     * @return 발행된 외부 포스트 URL
     */
    public String publish(BlogPost post) {
        String platform = post.getPlatform() == null ? "BOTH" : post.getPlatform();

        // [왜] 토큰이 없으면 dry-run — 실제 API 없이도 자동발행 흐름 전체를 테스트 가능
        if (tistorySession.isEmpty() && velogToken.isEmpty()) {
            log.info("[blog] dry-run 발행: title='{}', platform={}", post.getTitle(), platform);
            return "https://dry-run.example.com/posts/" + post.getId();
        }

        String result = "published";

        if (("TISTORY".equals(platform) || "BOTH".equals(platform)) && !tistorySession.isEmpty()) {
            result = postToTistory(post);
        }
        if (("VELOG".equals(platform) || "BOTH".equals(platform)) && !velogToken.isEmpty()) {
            result = postToVelog(post);
        }

        return result;
    }

    /**
     * Tistory Open API 발행.
     *
     * [왜] Tistory는 OAuth2 세션 쿠키(ts_session) 기반 API. 공개 API 문서:
     *      https://tistory.github.io/tistory-apis/
     * [어떻게] POST /apis/post/write → title·content·blogName·access_token 전달.
     * @DEEP 실제 요청 URL, access_token OAuth2 교환 흐름 구현 필요
     */
    private String postToTistory(BlogPost post) {
        String apiUrl = "https://www.tistory.com/apis/post/write";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // [왜] Tistory API는 ts_session 쿠키로 세션 인증
        headers.add(HttpHeaders.COOKIE, "ts_session=" + tistorySession);

        String body = "access_token=" + tistorySession
            + "&output=json"
            + "&blogName=" + tistoryBlogName
            + "&title=" + post.getTitle()
            + "&content=" + (post.getContent() != null ? post.getContent() : "")
            + "&visibility=3";   // 3 = 공개

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                apiUrl, new HttpEntity<>(body, headers), Map.class);
            Object url = resp.getBody() != null ? ((Map<?,?>) resp.getBody().get("tistory")).get("postUrl") : null;
            log.info("[blog] Tistory 발행 완료: {}", url);
            return url != null ? url.toString() : "https://" + tistoryBlogName + ".tistory.com";
        } catch (Exception e) {
            log.error("[blog] Tistory 발행 실패: {}", e.getMessage());
            throw new RuntimeException("Tistory 발행 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Velog GraphQL API 발행.
     *
     * [왜] Velog는 공식 REST API가 없고 GraphQL로만 노출됨.
     *      비공식 API지만 access-token 헤더로 인증.
     * [어떻게] POST https://v2.velog.io/graphql → writePost mutation.
     * @DEEP access-token 갱신 로직, GraphQL mutation 완전 구현 필요
     */
    private String postToVelog(BlogPost post) {
        String graphqlUrl = "https://v2.velog.io/graphql";

        // [왜] Velog GraphQL mutation: writePost
        String mutation = """
            mutation WritePost($input: WritePostInput!) {
              writePost(input: $input) { id url_slug }
            }
            """;

        Map<String, Object> variables = Map.of("input", Map.of(
            "title", post.getTitle(),
            "body",  post.getContent() != null ? post.getContent() : "",
            "tags",  post.getTags() != null ? post.getTags().split(",") : new String[0],
            "is_private", false
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", velogToken);

        Map<String, Object> payload = Map.of("query", mutation, "variables", variables);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                graphqlUrl, new HttpEntity<>(payload, headers), Map.class);
            log.info("[blog] Velog 발행 완료: user={}", velogUsername);
            return "https://velog.io/@" + velogUsername;
        } catch (Exception e) {
            log.error("[blog] Velog 발행 실패: {}", e.getMessage());
            throw new RuntimeException("Velog 발행 실패: " + e.getMessage(), e);
        }
    }
}
