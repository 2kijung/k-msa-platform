package com.kmsa.content.repository;

import com.kmsa.content.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * SkillRepository — portfolio 스키마 skills 테이블.
 * [MSA] content-service만 소유. category + displayOrder 복합 정렬로 기술 스택을 그룹별 표시.
 * @FROM  K-portfolio SkillRepository
 * @PHASE:3
 */
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByOrderByCategoryAscDisplayOrderAsc();
}
