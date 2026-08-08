package com.kmsa.content.repository;

import com.kmsa.content.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * CareerRepository — portfolio 스키마 careers 테이블.
 * [MSA] content-service만 소유. 다른 서비스는 GET /api/content/careers 로만 접근.
 * @FROM  K-portfolio CareerRepository
 * @PHASE:3
 */
public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findAllByOrderByDisplayOrderAsc();
}
