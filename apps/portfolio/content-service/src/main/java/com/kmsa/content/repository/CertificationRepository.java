package com.kmsa.content.repository;

import com.kmsa.content.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * CertificationRepository — portfolio 스키마 certifications 테이블.
 * [MSA] content-service만 소유. 취득일 역순 등이 아닌 displayOrder로 수동 순서 제어.
 * @FROM  K-portfolio CertificationRepository
 * @PHASE:3
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findAllByOrderByDisplayOrderAsc();
}
