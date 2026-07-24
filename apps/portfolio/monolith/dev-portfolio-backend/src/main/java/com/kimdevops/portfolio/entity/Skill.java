package com.kimdevops.portfolio.entity;

import jakarta.persistence.*;

/**
 * 기술 스택(Skill) 엔티티 = DB의 skills 테이블
 */
@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;          // 카테고리 (예: Backend, DevOps, Database)

    @Column(nullable = false)
    private String name;              // 기술명 (예: Spring Boot)

    @Column
    private Integer level = 80;       // 숙련도 0~100

    @Column
    private String color = "#3b82f6"; // 바 색상 (선택)

    @Column(columnDefinition = "TEXT")
    private String description;        // 툴팁 설명 (선택)

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    public Skill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
