package com.kmsa.blog.repository;

import com.kmsa.blog.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/** @PHASE:4 */
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findAllByOrderByDisplayOrderAsc();
    List<BlogPost> findByStatusOrderByCreatedAtDesc(BlogPost.Status status);
    // [왜] 예약 발행 스케줄러: 예약 시각이 지난 SCHEDULED 포스트 조회
    List<BlogPost> findByStatusAndScheduledAtBefore(BlogPost.Status status, LocalDateTime now);
    List<BlogPost> findByFeaturedTrueOrderByCreatedAtDesc();
}
