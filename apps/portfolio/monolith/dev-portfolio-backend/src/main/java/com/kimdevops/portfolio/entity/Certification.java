package com.kimdevops.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 자격증(Certification) 엔티티 = DB의 certifications 테이블
 */
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;              // 자격증명 (예: 정보처리기사)

    @Column
    private String issuer;            // 발급기관 (예: 한국산업인력공단)

    @Column(name = "acquired_date")
    private String acquiredDate;      // 취득일 (예: "2023-05")

    @Column
    private String score;             // 점수/등급 (선택, 예: "1급", "900점")

    @Column(name = "display_order")
    private Integer displayOrder = 0; // 표시 순서

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Certification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAcquiredDate() { return acquiredDate; }
    public void setAcquiredDate(String acquiredDate) { this.acquiredDate = acquiredDate; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
