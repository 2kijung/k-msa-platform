package com.kimdevops.portfolio.dto;

public class VisitorStatsDTO {
    private Long totalVisitors;
    private Long uniqueIPs;
    private Long uniqueSessions;
    private Long pageViews;

    public VisitorStatsDTO() {}

    public Long getTotalVisitors() { return totalVisitors; }
    public void setTotalVisitors(Long totalVisitors) { this.totalVisitors = totalVisitors; }

    public Long getUniqueIPs() { return uniqueIPs; }
    public void setUniqueIPs(Long uniqueIPs) { this.uniqueIPs = uniqueIPs; }

    public Long getUniqueSessions() { return uniqueSessions; }
    public void setUniqueSessions(Long uniqueSessions) { this.uniqueSessions = uniqueSessions; }

    public Long getPageViews() { return pageViews; }
    public void setPageViews(Long pageViews) { this.pageViews = pageViews; }
}
