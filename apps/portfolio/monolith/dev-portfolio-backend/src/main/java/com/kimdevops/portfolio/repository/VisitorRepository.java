package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByVisitedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM Visitor v WHERE v.visitedAt BETWEEN ?1 AND ?2")
    Long countUniqueSessions(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM Visitor v WHERE v.visitedAt BETWEEN ?1 AND ?2")
    Long countUniqueIPs(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT v.page, COUNT(v) FROM Visitor v WHERE v.visitedAt BETWEEN ?1 AND ?2 GROUP BY v.page ORDER BY COUNT(v) DESC")
    List<Object[]> getPageViews(LocalDateTime start, LocalDateTime end);
}
