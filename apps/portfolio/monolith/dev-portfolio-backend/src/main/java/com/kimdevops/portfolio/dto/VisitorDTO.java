package com.kimdevops.portfolio.dto;

import java.time.LocalDateTime;

public class VisitorDTO {
    private Long id;
    private String ipAddress;
    private String page;
    private String country;
    private String city;
    private LocalDateTime visitedAt;

    public VisitorDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public LocalDateTime getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDateTime visitedAt) { this.visitedAt = visitedAt; }
}

class VisitorRequest {
    private String ipAddress;
    private String userAgent;
    private String referer;
    private String page;
    private String sessionId;

    public VisitorRequest() {}

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}

