package com.kmsa.template.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * TemplateService — 비즈니스 로직 골격.
 *
 * [패턴]
 * - 클래스 레벨 @Transactional(readOnly=true) → 조회 메서드 기본 readOnly
 * - 쓰기 메서드는 개별 @Transactional 오버라이드
 * - [왜 readOnly=true] JPA dirty checking 생략 → 조회 성능 향상 + 실수로 수정 방지
 */
@Service
@Transactional(readOnly = true)
public class TemplateService {

    public List<?> findAll() {
        // [TODO] repository.findAll() 또는 커스텀 쿼리
        return Collections.emptyList();
    }

    @Transactional
    public Object create(Object body) {
        // [TODO] entity 변환 → repository.save()
        return body;
    }
}
