package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Career;
import com.kimdevops.portfolio.repository.CareerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CareerService — 모놀리스(K-portfolio)의 Career CRUD 서비스.
 *
 * <p><b>Strangler Fig 전환 상태:</b> Phase 3에서 content-service로 분리 예정</p>
 * <p>이 클래스는 MSA 전환 중 잠시 공존하는 레거시 코드.
 * 게이트웨이가 해당 도메인의 라우트를 신규 서비스로 전환하면 이 코드는 사용되지 않는다.
 * 그때까지는 monolith가 fallback 역할을 수행한다.</p>
 *
 * @FROM  K-portfolio 원본 서비스 (Phase 0 흡수)
 * @RISK  이 서비스를 수정하면 신규 서비스와 로직이 달라질 수 있음 — 분리 완료 후 삭제 대상
 */
@Service
public class CareerService {
    @Autowired
    private CareerRepository careerRepository;

    @Transactional(readOnly = true)
    public List<Career> getAll() {
        return careerRepository.findAllByOrderByDisplayOrderAscStartDateDesc();
    }

    @Transactional
    public Career create(Career input) {
        Career career = new Career();
        career.setCompany(input.getCompany());
        career.setPosition(input.getPosition());
        career.setStartDate(input.getStartDate());
        career.setEndDate(input.getEndDate());
        career.setDescription(input.getDescription());
        career.setDisplayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0);
        return careerRepository.save(career);
    }

    @Transactional
    public void delete(Long id) {
        careerRepository.deleteById(id);
    }
}
