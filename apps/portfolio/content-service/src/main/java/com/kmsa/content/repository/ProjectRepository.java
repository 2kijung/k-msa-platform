package com.kmsa.content.repository;

import com.kmsa.content.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** @FROM K-portfolio ProjectRepository — 메서드 이름 기반 쿼리 그대로 유지 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrderByDisplayOrderAsc();
    List<Project> findByFeaturedTrueOrderByDisplayOrderAsc();
    List<Project> findByStatusOrderByDisplayOrderAsc(Project.Status status);
}
