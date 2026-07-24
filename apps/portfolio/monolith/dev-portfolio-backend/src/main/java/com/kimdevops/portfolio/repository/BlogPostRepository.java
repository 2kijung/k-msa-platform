package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.BlogPost;
import com.kimdevops.portfolio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);
    Page<BlogPost> findByStatus(BlogPost.Status status, Pageable pageable);
    Page<BlogPost> findByCategory(String category, Pageable pageable);
    Page<BlogPost> findByStatusAndAuthor(BlogPost.Status status, User author, Pageable pageable);
    
    @Query("SELECT p FROM BlogPost p WHERE p.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
    Page<BlogPost> findPublishedPosts(Pageable pageable);
    
    @Query("SELECT p FROM BlogPost p WHERE p.status = 'PUBLISHED' AND p.category = ?1 ORDER BY p.publishedAt DESC")
    Page<BlogPost> findPublishedPostsByCategory(String category, Pageable pageable);
}
