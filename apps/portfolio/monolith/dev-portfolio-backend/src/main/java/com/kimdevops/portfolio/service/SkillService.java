package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Skill;
import com.kimdevops.portfolio.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SkillService — 모놀리스(K-portfolio)의 Skill CRUD 서비스.
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
public class SkillService {
    @Autowired
    private SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<Skill> getAll() {
        return skillRepository.findAllByOrderByDisplayOrderAscIdAsc();
    }

    @Transactional
    public Skill create(Skill input) {
        Skill skill = new Skill();
        skill.setCategory(input.getCategory());
        skill.setName(input.getName());
        skill.setLevel(input.getLevel() != null ? input.getLevel() : 80);
        skill.setColor(input.getColor() != null ? input.getColor() : "#3b82f6");
        skill.setDisplayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0);
        return skillRepository.save(skill);
    }

    @Transactional
    public void delete(Long id) {
        skillRepository.deleteById(id);
    }
}
