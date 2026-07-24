package com.kimdevops.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 개발 노트(DevNote) 엔티티 = DB의 dev_notes 테이블
 * 이 프로젝트를 어떻게 설계/구현했는지, 어떤 문제를 어떻게 해결했는지 기록 (트러블슈팅 케이스)
 */
@Entity
@Table(name = "dev_notes")
public class DevNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;             // 제목 (예: CORS 에러 해결)

    @Column
    private String category;          // 카테고리 (설계 / 기술선택 / 트러블슈팅 / DB)

    @Column(columnDefinition = "TEXT")
    private String situation;         // 상황/문제 (처음엔 이렇게 했더니 오류...)

    @Column(name = "code_before", columnDefinition = "TEXT")
    private String codeBefore;        // 문제가 된 코드 (선택)

    @Column(name = "code_after", columnDefinition = "TEXT")
    private String codeAfter;         // 수정한 코드 (선택)

    @Column(columnDefinition = "TEXT")
    private String solution;          // 원인 분석 + 해결 + 교훈

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DevNote() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }

    public String getCodeBefore() { return codeBefore; }
    public void setCodeBefore(String codeBefore) { this.codeBefore = codeBefore; }

    public String getCodeAfter() { return codeAfter; }
    public void setCodeAfter(String codeAfter) { this.codeAfter = codeAfter; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
