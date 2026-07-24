package com.kmsa.contact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Contact 엔티티 — contact 스키마의 contacts 테이블
 * [무엇을] 방문자가 남긴 문의. K-portfolio(monolith)의 Contact를 이관.
 * [왜/MSA] 문의 데이터는 contact-service만 소유. 다른 서비스는 API로만 접근.
 */
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 문의 처리 상태 (신규→읽음→답변→보관) */
    public enum Status { NEW, READ, REPLIED, ARCHIVED }

    protected Contact() {}

    public Contact(String name, String email, String subject, String message) {
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
