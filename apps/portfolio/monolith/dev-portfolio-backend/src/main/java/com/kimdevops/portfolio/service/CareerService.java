package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Career;
import com.kimdevops.portfolio.repository.CareerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
