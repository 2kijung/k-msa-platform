package com.kmsa.blog.service;

import com.kmsa.blog.entity.BlogPost;
import com.kmsa.blog.repository.BlogPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * BlogService — 블로그 포스트 관리 + 예약 발행 스케줄러.
 *
 * @FROM  K-portfolio BlogPost/DevNote + openclaw PostingScheduler
 * @HOW   CRUD + 자동발행(TistoryApiClient·VelogApiClient stub).
 * @PHASE:4
 * @DEEP  TistoryApiClient·VelogApiClient 실제 구현 (openclaw blog-service에서 이관)
 */
@Service
@Transactional(readOnly = true)
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogPostRepository blogPostRepo;
    private final PostingClient postingClient;

    public BlogService(BlogPostRepository blogPostRepo, PostingClient postingClient) {
        this.blogPostRepo = blogPostRepo;
        this.postingClient = postingClient;
    }

    public List<BlogPost> findAll() {
        return blogPostRepo.findAllByOrderByDisplayOrderAsc();
    }

    public List<BlogPost> findPublished() {
        return blogPostRepo.findByStatusOrderByCreatedAtDesc(BlogPost.Status.PUBLISHED);
    }

    public Optional<BlogPost> findById(Long id) {
        return blogPostRepo.findById(id);
    }

    @Transactional
    public BlogPost save(BlogPost post) {
        post.setUpdatedAt(LocalDateTime.now());
        return blogPostRepo.save(post);
    }

    @Transactional
    public void delete(Long id) {
        blogPostRepo.deleteById(id);
    }

    /**
     * 예약 발행 스케줄러. 30분마다 실행.
     * [왜/MSA] 블로그 발행이 본 서비스(contact·content) 배포 주기와 독립 → blog-service로 분리한 이유.
     *
     * @DEEP TistoryApiClient·VelogApiClient 실제 HTTP 구현 추가 (openclaw 참조)
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    @Transactional
    public void publishScheduled() {
        List<BlogPost> ready = blogPostRepo.findByStatusAndScheduledAtBefore(
            BlogPost.Status.SCHEDULED, LocalDateTime.now()
        );
        for (BlogPost post : ready) {
            try {
                String url = postingClient.publish(post);
                post.setStatus(BlogPost.Status.PUBLISHED);
                post.setPublishedAt(LocalDateTime.now());
                post.setExternalUrl(url);
                blogPostRepo.save(post);
                log.info("[blog] 예약 발행 성공: id={}, title={}", post.getId(), post.getTitle());
            } catch (Exception e) {
                post.setStatus(BlogPost.Status.FAILED);
                blogPostRepo.save(post);
                log.error("[blog] 예약 발행 실패: id={}", post.getId(), e);
            }
        }
    }
}
