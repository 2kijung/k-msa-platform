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
