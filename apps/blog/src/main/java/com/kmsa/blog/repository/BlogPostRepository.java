package com.kmsa.blog.repository;

import com.kmsa.blog.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BlogPostRepository — blog 스키마의 blog_posts 테이블 접근.
 *
 * <p><b>MSA에서의 역할:</b>
 * blog-service만 이 레포지토리에 접근한다. 다른 서비스가 블로그 포스트를 조회하려면
 * GET /api/blog/posts REST API를 통해야 한다(직접 DB 접근 금지 = 데이터 소유 원칙).</p>
 *
 * <p><b>메서드 명명 규칙:</b>
 * Spring Data JPA가 메서드명으로 쿼리를 자동 생성한다. SQL 작성 없이 조건부 조회 가능.
 * {@code findByStatusAndScheduledAtBefore}는 다음 SQL을 생성한다:
 * {@code WHERE status = ? AND scheduled_at < ?}</p>
 *
 * @PHASE:4 Phase 4 — monolith에서 BlogPost 도메인 분리
 * @SCALE  읽기 많은 포스트 목록은 추후 Redis @Cacheable 적용 포인트
 */
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    /** 전체 목록: displayOrder 오름차순 (사람이 지정한 순서대로 보임) */
    List<BlogPost> findAllByOrderByDisplayOrderAsc();

    /** 발행 완료(PUBLISHED) 포스트만: 최신순 (외부 블로그 게시용) */
    List<BlogPost> findByStatusOrderByCreatedAtDesc(BlogPost.Status status);

    /**
     * 예약 발행 스케줄러가 호출 — 현재 시각보다 scheduledAt이 이전인 SCHEDULED 포스트 반환.
     * [왜] @Scheduled(fixedDelay=30분)로 주기 실행 → 예약 시각이 지난 포스트를 Tistory/Velog에 발행.
     */
    List<BlogPost> findByStatusAndScheduledAtBefore(BlogPost.Status status, LocalDateTime now);

    /** 주요 포스트만: featured=true, 최신순 */
    List<BlogPost> findByFeaturedTrueOrderByCreatedAtDesc();
}
