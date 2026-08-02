package com.kmsa.content.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 기본정보(Profile) 엔티티 → portfolio 스키마의 profile 테이블.
 * 단일 행 (포트폴리오 주인의 인적사항).
 *
 * @FROM  K-portfolio entity/Profile.java
 * @PHASE:3
 */
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "birth_date")
    private String birthDate;

    @Column
    private String location;

    @Column
    private String university;

    @Column
    private String major;

    @Column(name = "graduation_status")
    private String graduationStatus;

    @Column(name = "military_status")
    private String militaryStatus;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column
    private String email;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "blog_url")
    private String blogUrl;

    @Column(name = "tistory_url")
    private String tistoryUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "current_status", columnDefinition = "TEXT")
    private String currentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Profile() {}

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
