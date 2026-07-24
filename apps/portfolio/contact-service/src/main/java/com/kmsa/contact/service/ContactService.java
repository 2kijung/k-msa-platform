package com.kmsa.contact.service;

import com.kmsa.contact.dto.ContactDtos.ContactRequest;
import com.kmsa.contact.entity.Contact;
import com.kmsa.contact.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ContactService — 문의 접수/조회 비즈니스 로직
 * [흐름] 문의 저장(DB) → notification-service에 알림 요청.
 * [왜/MSA] 저장은 자기 DB(contact 스키마), 알림은 다른 서비스 호출(REST). 책임 분리.
 */
@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final NotificationClient notificationClient;

    public ContactService(ContactRepository contactRepository, NotificationClient notificationClient) {
        this.contactRepository = contactRepository;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public Contact submit(ContactRequest req) {
        // 1) 문의 저장 (자기 DB)
        Contact saved = contactRepository.save(
            new Contact(req.name(), req.email(), req.subject(), req.message()));

        // 2) 알림 요청 (다른 서비스 REST 호출). [왜] 실패해도 위 저장은 이미 커밋됨 → 접수 성공 보장.
        notificationClient.send("새 문의 도착: [" + req.name() + "] " + req.subject());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Contact> findAll() {
        return contactRepository.findAll();
    }
}
