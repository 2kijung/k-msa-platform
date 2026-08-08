package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.dto.ContactDTO;
import com.kimdevops.portfolio.entity.Contact;
import com.kimdevops.portfolio.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * ContactService — 모놀리스(K-portfolio)의 Contact CRUD 서비스.
 *
 * <p><b>Strangler Fig 전환 상태:</b> Phase 2에서 contact-service로 이미 분리됨 → 이 코드는 레거시</p>
 * <p>이 클래스는 MSA 전환 중 잠시 공존하는 레거시 코드.
 * 게이트웨이가 해당 도메인의 라우트를 신규 서비스로 전환하면 이 코드는 사용되지 않는다.
 * 그때까지는 monolith가 fallback 역할을 수행한다.</p>
 *
 * @FROM  K-portfolio 원본 서비스 (Phase 0 흡수)
 * @RISK  이 서비스를 수정하면 신규 서비스와 로직이 달라질 수 있음 — 분리 완료 후 삭제 대상
 */
@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Transactional
    public ContactDTO createContact(String name, String email, String subject, String message) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        contact.setSubject(subject);
        contact.setMessage(message);
        contact.setStatus(Contact.Status.NEW);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        
        Contact saved = contactRepository.save(contact);
        return toContactDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactDTO> getContacts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable)
                .map(this::toContactDTO);
    }

    @Transactional
    public ContactDTO updateStatus(Long id, String status) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        contact.setStatus(Contact.Status.valueOf(status));
        contact.setUpdatedAt(LocalDateTime.now());
        Contact updated = contactRepository.save(contact);
        return toContactDTO(updated);
    }

    private ContactDTO toContactDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setSubject(contact.getSubject());
        dto.setMessage(contact.getMessage());
        dto.setStatus(contact.getStatus().name());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());
        return dto;
    }
}
