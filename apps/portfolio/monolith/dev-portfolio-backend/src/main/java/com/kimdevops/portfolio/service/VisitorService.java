package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.dto.VisitorDTO;
import com.kimdevops.portfolio.dto.VisitorStatsDTO;
import com.kimdevops.portfolio.entity.Visitor;
import com.kimdevops.portfolio.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

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
