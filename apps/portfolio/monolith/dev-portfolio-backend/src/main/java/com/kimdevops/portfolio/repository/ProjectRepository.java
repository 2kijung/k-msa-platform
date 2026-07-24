package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderByDisplayOrder(Project.Status status);
    List<Project> findByFeaturedOrderByDisplayOrder(Boolean featured);
    Page<Project> findByStatus(Project.Status status, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.status = 'PRODUCTION' ORDER BY p.displayOrder ASC")
    List<Project> findProductionProjects();
}
