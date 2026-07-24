package com.kmsa.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * User 엔티티 — auth 스키마의 users 테이블
 * ============================================================================
 * [무엇을] 로그인 대상 사용자. K-portfolio(monolith)의 User를 그대로 이관.
 * [왜/MSA] MSA의 3원칙 = "데이터 소유". 사용자 데이터는 auth-service만 소유하고 쓴다.
 *          다른 서비스가 사용자 정보를 알고 싶으면 auth-service의 API를 호출해야지,
 *          auth 스키마의 users 테이블을 직접 조회하면 안 된다(분산 모놀리스 금지).
 * [어떻게] jakarta.persistence(Boot 3 = jakarta). monolith와 동일 구조라 이관이 자연스럽다.
 * ============================================================================
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    /** [보안] 평문이 아니라 BCrypt 해시가 저장된다(AuthService에서 encode). */
    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * [왜 중요] role은 JWT에 클레임으로 담겨 게이트웨이까지 전파된다.
     * 원본(K-portfolio)은 이 role을 인가에 안 써서 "유효 토큰이면 무조건 관리자"였다.
     * auth-service에서는 이 값을 토큰에 실어 보내 "권한을 근거로" 판단하게 교정한다.
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { ADMIN, USER }

    protected User() {}   // JPA 기본 생성자

    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // --- Getters (setter는 불변성 위해 최소화; 필요한 것만) ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
