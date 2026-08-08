package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.dto.VisitorDTO;
import com.kimdevops.portfolio.dto.VisitorStatsDTO;
import com.kimdevops.portfolio.entity.Visitor;
import com.kimdevops.portfolio.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * VisitorService — 모놀리스(K-portfolio)의 Visitor 기록 서비스.
 *
 * <p><b>Strangler Fig 전환 상태:</b> Phase 5에서 analytics-service로 분리 예정</p>
 * <p>이 클래스는 MSA 전환 중 잠시 공존하는 레거시 코드.
 * 게이트웨이가 해당 도메인의 라우트를 신규 서비스로 전환하면 이 코드는 사용되지 않는다.
 * 그때까지는 monolith가 fallback 역할을 수행한다.</p>
 *
 * @FROM  K-portfolio 원본 서비스 (Phase 0 흡수)
 * @RISK  이 서비스를 수정하면 신규 서비스와 로직이 달라질 수 있음 — 분리 완료 후 삭제 대상
 */
@Service
public class VisitorService {
    @Autowired
    private VisitorRepository visitorRepository;

    @Transactional
    public VisitorDTO recordVisit(String ipAddress, String userAgent, String referer, String page, String sessionId) {
        Visitor visitor = new Visitor();
        visitor.setIpAddress(ipAddress);
        visitor.setUserAgent(userAgent);
        visitor.setReferer(referer);
        visitor.setPage(page);
        visitor.setSessionId(sessionId);
        visitor.setVisitedAt(LocalDateTime.now());
        
        Visitor saved = visitorRepository.save(visitor);
        return toVisitorDTO(saved);
    }

    @Transactional(readOnly = true)
    public VisitorStatsDTO getStats(LocalDateTime start, LocalDateTime end) {
        long totalVisitors = visitorRepository.count();
        long uniqueIPs = visitorRepository.countUniqueIPs(start, end);
        long uniqueSessions = visitorRepository.countUniqueSessions(start, end);
        long pageViews = visitorRepository.findByVisitedAtBetween(start, end).size();
        
        VisitorStatsDTO stats = new VisitorStatsDTO();
        stats.setTotalVisitors(totalVisitors);
        stats.setUniqueIPs(uniqueIPs);
        stats.setUniqueSessions(uniqueSessions);
        stats.setPageViews(pageViews);
        return stats;
    }

    private VisitorDTO toVisitorDTO(Visitor visitor) {
        VisitorDTO dto = new VisitorDTO();
        dto.setId(visitor.getId());
        dto.setIpAddress(visitor.getIpAddress());
        dto.setPage(visitor.getPage());
        dto.setCountry(visitor.getCountry());
        dto.setCity(visitor.getCity());
        dto.setVisitedAt(visitor.getVisitedAt());
        return dto;
    }
}
