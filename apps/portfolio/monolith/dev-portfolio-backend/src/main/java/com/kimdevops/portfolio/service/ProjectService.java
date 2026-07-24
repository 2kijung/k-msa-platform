package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.dto.ProjectDTO;
import com.kimdevops.portfolio.entity.Project;
import com.kimdevops.portfolio.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public ProjectDTO createProject(String title, String description, String technologies, String status) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setTechnologies(technologies);
        project.setStatus(Project.Status.valueOf(status));
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        Project saved = projectRepository.save(project);
        return toProjectDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProductionProjects() {
        return projectRepository.findProductionProjects()
                .stream()
                .map(this::toProjectDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository.findAll(pageable)
                .map(this::toProjectDTO);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, String title, String description, String status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setTitle(title);
        project.setDescription(description);
        project.setStatus(Project.Status.valueOf(status));
        project.setUpdatedAt(LocalDateTime.now());
        
        Project updated = projectRepository.save(project);
        return toProjectDTO(updated);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private ProjectDTO toProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setLongDescription(project.getLongDescription());
        dto.setTechnologies(project.getTechnologies());
        dto.setStatus(project.getStatus().name());
        dto.setImageUrl(project.getImageUrl());
        dto.setGithubUrl(project.getGithubUrl());
        dto.setLiveUrl(project.getLiveUrl());
        dto.setMetrics(project.getMetrics());
        dto.setFeatured(project.getFeatured());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}
