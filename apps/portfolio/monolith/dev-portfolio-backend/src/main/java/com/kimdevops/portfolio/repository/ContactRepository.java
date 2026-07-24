package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Page<Contact> findByStatus(Contact.Status status, Pageable pageable);
    Page<Contact> findAll(Pageable pageable);
    List<Contact> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
