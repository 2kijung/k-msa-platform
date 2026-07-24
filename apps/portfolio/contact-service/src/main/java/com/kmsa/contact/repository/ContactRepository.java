package com.kmsa.contact.repository;

import com.kmsa.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/** [무엇을] contacts 테이블 접근. [왜/MSA] contact-service 내부에만 존재(경계 강제). */
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
