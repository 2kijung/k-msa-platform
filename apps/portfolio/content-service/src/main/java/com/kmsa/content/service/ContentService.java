package com.kmsa.content.service;

import com.kmsa.content.entity.*;
import com.kmsa.content.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ContentService — 포트폴리오 콘텐츠 도메인 서비스.
 *
 * @FROM  K-portfolio의 각 도메인 서비스를 하나로 통합(Project/Career/Skill/Cert/Profile).
 * @HOW   조회는 Transactional(readOnly). 쓰기는 일반 Transactional.
 * @PHASE:3
 * @SCALE 읽기 캐시(@Cacheable)는 @DEEP — 트래픽이 늘면 Redis 도입 포인트.
 */
@Service
@Transactional(readOnly = true)
public class ContentService {

    private final ProjectRepository projectRepo;
    private final CareerRepository careerRepo;
    private final SkillRepository skillRepo;
    private final CertificationRepository certRepo;
    private final ProfileRepository profileRepo;

    public ContentService(ProjectRepository projectRepo,
                          CareerRepository careerRepo,
                          SkillRepository skillRepo,
                          CertificationRepository certRepo,
                          ProfileRepository profileRepo) {
        this.projectRepo = projectRepo;
        this.careerRepo = careerRepo;
        this.skillRepo = skillRepo;
        this.certRepo = certRepo;
        this.profileRepo = profileRepo;
    }

    // ──────── Project ────────

    public List<Project> findAllProjects() {
        return projectRepo.findAllByOrderByDisplayOrderAsc();
    }

    public List<Project> findFeaturedProjects() {
        return projectRepo.findByFeaturedTrueOrderByDisplayOrderAsc();
    }

    public Optional<Project> findProject(Long id) {
        return projectRepo.findById(id);
    }

    @Transactional
    public Project saveProject(Project project) {
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepo.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepo.deleteById(id);
    }

    // ──────── Career ────────

    public List<Career> findAllCareers() {
        return careerRepo.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Career saveCareer(Career career) {
        return careerRepo.save(career);
    }

    @Transactional
    public void deleteCareer(Long id) {
        careerRepo.deleteById(id);
    }

    // ──────── Skill ────────

    public List<Skill> findAllSkills() {
        return skillRepo.findAllByOrderByCategoryAscDisplayOrderAsc();
    }

    @Transactional
    public Skill saveSkill(Skill skill) {
        return skillRepo.save(skill);
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillRepo.deleteById(id);
    }

    // ──────── Certification ────────

    public List<Certification> findAllCertifications() {
        return certRepo.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Certification saveCertification(Certification cert) {
        return certRepo.save(cert);
    }

    @Transactional
    public void deleteCertification(Long id) {
        certRepo.deleteById(id);
    }

    // ──────── Profile (단일 행) ────────

    public Optional<Profile> findProfile() {
        return profileRepo.findTopByOrderByIdAsc();
    }

    @Transactional
    public Profile saveProfile(Profile profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepo.save(profile);
    }
}
