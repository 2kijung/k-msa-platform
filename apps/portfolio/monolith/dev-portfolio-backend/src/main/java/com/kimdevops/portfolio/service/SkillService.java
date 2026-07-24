package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Skill;
import com.kimdevops.portfolio.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
