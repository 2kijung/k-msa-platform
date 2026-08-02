package com.kmsa.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Visitor — 방문자 기록. analytics 스키마.
 *
 * @FROM  K-portfolio entity/Visitor.java
 * @PHASE:5
 */
@Entity
@Table(name = "visitors")
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "page_path")
    private String pagePath;

    @Column(name = "referer")
    private String referer;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt = LocalDateTime.now();

    public Visitor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getPagePath() { return pagePath; }
    public void setPagePath(String pagePath) { this.pagePath = pagePath; }
    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }
    public LocalDateTime getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDateTime visitedAt) { this.visitedAt = visitedAt; }
}
