package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Certification;
import com.kimdevops.portfolio.repository.CertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    @Transactional(readOnly = true)
    public List<Certification> getAll() {
        return certificationRepository.findAllByOrderByDisplayOrderAscAcquiredDateDesc();
    }

    @Transactional
    public Certification create(Certification input) {
        // id는 자동 생성되므로 새 엔티티로 저장
        Certification cert = new Certification();
        cert.setName(input.getName());
        cert.setIssuer(input.getIssuer());
        cert.setAcquiredDate(input.getAcquiredDate());
        cert.setScore(input.getScore());
        cert.setDisplayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0);
        return certificationRepository.save(cert);
    }

    @Transactional
    public void delete(Long id) {
        certificationRepository.deleteById(id);
    }
}
