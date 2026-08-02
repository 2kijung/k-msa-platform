package com.kmsa.content.repository;

import com.kmsa.content.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByOrderByCategoryAscDisplayOrderAsc();
}
