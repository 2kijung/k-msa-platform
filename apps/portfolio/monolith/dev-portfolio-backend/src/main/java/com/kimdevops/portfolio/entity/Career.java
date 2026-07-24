package com.kimdevops.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 경력(Career) 엔티티 = DB의 careers 테이블
 * 회사 재직 이력 (어느 회사에서 무슨 일을 얼마나)
 */
@Entity
@Table(name = "careers")
public class Career {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;           // 회사명

    @Column
    private String position;          // 직무/직책 (예: 백엔드 개발자)

    @Column(name = "start_date")
    private String startDate;         // 입사 (예: "2021-03")

    @Column(name = "end_date")
    private String endDate;           // 퇴사 (예: "2023-12", 비우면 재직중)

    @Column(columnDefinition = "TEXT")
    private String description;       // 주요 업무/성과

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Career() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
