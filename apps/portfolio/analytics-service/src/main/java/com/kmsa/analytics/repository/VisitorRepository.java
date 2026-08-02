package com.kmsa.analytics.repository;

import com.kmsa.analytics.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

/** @PHASE:5 @DEEP 집계 쿼리: 일별·월별 방문자 수, 페이지별 방문 수 */
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    long countByVisitedAtBetween(LocalDateTime from, LocalDateTime to);
}
