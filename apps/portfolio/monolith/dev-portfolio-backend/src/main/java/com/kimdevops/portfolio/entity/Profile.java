package com.kimdevops.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 기본정보(Profile) 엔티티 = DB의 profile 테이블
 * 포트폴리오 주인의 인적사항/학력/병역/소개 정보를 담는다.
 * (한 명의 프로필만 존재 - 단일 행)
 */
/**
 * Profile 엔티티 — 모놀리스 레거시 (Phase 3 → content-service 이관됨).
 * 게이트웨이 라우팅 전환 후에는 신규 서비스의 동일 이름 엔티티가 주체가 된다.
 * @RISK  신규 서비스 엔티티와 필드가 다를 수 있음 — 이관 완료 후 이 클래스 삭제
 */
@Entity
@Table(name = "profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 인적사항 ---
    @Column(nullable = false)
    private String name;              // 이름

    @Column(name = "birth_date")
    private String birthDate;         // 생년(월일) 예: "1995" 또는 "1995-03-15"

    @Column
    private String location;          // 거주지 예: "서울특별시"

    // --- 학력 ---
    @Column
    private String university;        // 대학교

    @Column
    private String major;             // 전공

    @Column(name = "graduation_status")
    private String graduationStatus;  // 졸업여부: 졸업 / 재학 / 휴학 / 중퇴

    // --- 병역 ---
    @Column(name = "military_status")
    private String militaryStatus;    // 병역: 군필 / 미필 / 면제 / 해당없음

    // --- 소개/연락처 ---
    @Column(columnDefinition = "TEXT")
    private String introduction;      // 한줄 소개

    @Column
    private String email;             // 이메일

    @Column(name = "github_url")
    private String githubUrl;         // GitHub 주소

    @Column(name = "blog_url")
    private String blogUrl;           // 블로그 주소 (선택)

    @Column(name = "tistory_url")
    private String tistoryUrl;        // 티스토리 블로그 주소

    @Column(name = "image_url")
    private String imageUrl;          // 프로필 사진 URL (업로드된 이미지)

    @Column(name = "current_status", columnDefinition = "TEXT")
    private String currentStatus;     // 현재 준비/진행 중인 것

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Profile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGraduationStatus() { return graduationStatus; }
    public void setGraduationStatus(String graduationStatus) { this.graduationStatus = graduationStatus; }

    public String getMilitaryStatus() { return militaryStatus; }
    public void setMilitaryStatus(String militaryStatus) { this.militaryStatus = militaryStatus; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getBlogUrl() { return blogUrl; }
    public void setBlogUrl(String blogUrl) { this.blogUrl = blogUrl; }

    public String getTistoryUrl() { return tistoryUrl; }
    public void setTistoryUrl(String tistoryUrl) { this.tistoryUrl = tistoryUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
