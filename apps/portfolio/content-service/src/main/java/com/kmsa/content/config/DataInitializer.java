package com.kmsa.content.config;

import com.kmsa.content.entity.*;
import com.kmsa.content.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DataInitializer — content-service 시작 시 portfolio 스키마에 기본 데이터 삽입.
 *
 * @FROM  K-portfolio DataInitializer(content 관련 섹션만 추출)
 * @HOW   최초 기동 시 비어 있으면 삽입. 재기동 시 count()>0 이면 건너뜀(멱등).
 * @PHASE:3
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepo;
    private final CareerRepository careerRepo;
    private final SkillRepository skillRepo;
    private final CertificationRepository certRepo;
    private final ProfileRepository profileRepo;

    public DataInitializer(ProjectRepository projectRepo,
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

    @Override
    public void run(String... args) {
        if (profileRepo.count() == 0) initProfile();
        if (certRepo.count() == 0)    initCertifications();
        if (careerRepo.count() == 0)  initCareers();
        if (skillRepo.count() == 0)   initSkills();
        if (projectRepo.count() == 0) initProjects();
    }

    private void initProfile() {
        Profile p = new Profile();
        p.setName("이기정");
        p.setIntroduction("외부 시스템 연계와 데이터 정합성 중심으로 일해온 Java 백엔드 개발자. "
            + "4년 7개월간 12건의 SI 프로젝트에서 Spring Framework·MyBatis 기반 개발을 수행. "
            + "컨테이너 환경(K8s·Docker·Jenkins)과 Spring Boot 3.2는 개인 프로젝트로 직접 구축·운영 중.");
        p.setEmail("dlrlwjd1313@gmail.com");
        p.setGithubUrl("https://github.com/2kijung");
        p.setTistoryUrl("https://dlrlwjd1313.tistory.com");
        p.setBlogUrl("https://velog.io/@dlrlwjd1313/posts");
        p.setCurrentStatus("이직 활동 중 — CJ·네이버웹툰·올리브영 등 서비스 회사 지원");
        p.setCreatedAt(LocalDateTime.now());
        profileRepo.save(p);
    }

    private void initCertifications() {
        // 실제 취득일 기준 (2026-08-02 확인)
        String[][] certs = {
            {"정보처리기사",      "한국산업인력공단",    "2024-09", ""},
            {"SQLD (SQL 개발자)", "한국데이터산업진흥원", "2024-04", ""},
            {"리눅스마스터 2급",  "KAIT",              "2022-11", "2급"},
        };
        int order = 1;
        for (String[] c : certs) {
            Certification cert = new Certification();
            cert.setName(c[0]);
            cert.setIssuer(c[1]);
            cert.setAcquiredDate(c[2]);
            cert.setScore(c[3]);
            cert.setDisplayOrder(order++);
            certRepo.save(cert);
        }
    }

    private void initCareers() {
        Career c = new Career();
        c.setCompany("엠로 (Emro)");
        c.setPosition("Java Backend Developer / 선임");
        c.setStartDate("2022-02");
        c.setEndDate("");
        c.setDescription("국내 구매(SRM) 솔루션 전문기업. 제조·금융·공공 대기업 SI 프로젝트에서 "
            + "외부 시스템 연계(SAP ERP·금융 API 등 20종 이상), 대량 데이터 이관, "
            + "레거시 컨버전, 운영 장애 분석을 수행.");
        c.setDisplayOrder(1);
        careerRepo.save(c);
    }

    private void initSkills() {
        Object[][] data = {
            {"Backend",  "Java",                 85, "#f97316"},
            {"Backend",  "Spring Framework",     85, "#6db33f"},
            {"Backend",  "Spring Boot",          82, "#6db33f"},
            {"Backend",  "JPA",                  80, "#59666c"},
            {"Backend",  "MyBatis",              82, "#8a6d3b"},
            {"Database", "Oracle",               82, "#f80000"},
            {"Database", "PostgreSQL",           78, "#336791"},
            {"Database", "MSSQL",                76, "#a91d22"},
            {"Frontend", "React",                72, "#61dafb"},
            {"Frontend", "TypeScript",           70, "#3178c6"},
            {"DevOps",   "Kubernetes",           75, "#326ce5"},
            {"DevOps",   "Docker",               75, "#2496ed"},
            {"DevOps",   "Jenkins",              75, "#d33833"},
            {"DevOps",   "Git",                  82, "#f05032"},
            {"DevOps",   "Maven",                78, "#c71a36"},
        };
        int order = 1;
        for (Object[] d : data) {
            Skill s = new Skill();
            s.setCategory((String) d[0]);
            s.setName((String) d[1]);
            s.setLevel((Integer) d[2]);
            s.setColor((String) d[3]);
            s.setDisplayOrder(order++);
            skillRepo.save(s);
        }
    }

    private void initProjects() {
        // K-portfolio (현재 운영 중)
        save("K-portfolio — 개인 포트폴리오 DevOps 운영",
            "Spring Boot 3.2 + React 19 모놀리식 포트폴리오 사이트. "
            + "Docker 멀티스테이지 빌드 + Jenkins CI/CD + Kubernetes(minikube) 운영 중. "
            + "Rolling Update·HPA·Liveness/Readiness Probe·Prometheus·Grafana 직접 구성.",
            "Spring Boot 3.2, React 19, PostgreSQL, Docker, Kubernetes, Jenkins, Prometheus, Grafana",
            "https://github.com/2kijung/K-portfolio",
            "https://k-devops.duckdns.org",
            true, 1);

        // k-msa-platform (전환 진행 중)
        save("k-msa-platform — Strangler Fig MSA 전환 플랫폼",
            "K-portfolio 모놀리스를 MSA로 점진 전환. "
            + "Phase 0~3 완료(auth·contact·notification·content 분리). "
            + "Nginx API 게이트웨이·JWT 중앙 인증·서비스별 PostgreSQL 스키마 분리. "
            + "k6 부하테스트 + Prometheus/Grafana 관측성 구성.",
            "Spring Boot 3.2, Nginx API Gateway, PostgreSQL, Docker Compose, Prometheus, Grafana, k6",
            "https://github.com/2kijung/k-msa-platform",
            null,
            true, 2);

        // openclaw-msa
        save("openclaw-msa — 가계부·블로그 자동화 MSA",
            "Spring Framework 5.3 Maven 멀티모듈 MSA. "
            + "budget-service(Spring Batch 메일 파싱·가계부 집계) / blog-service(Tistory·Velog 자동발행) / "
            + "notification-service(Telegram 알림). "
            + "Nginx auth_request 단일 인증 관문. K8s 매니페스트·Jenkinsfile 포함.",
            "Spring Framework 5.3, Maven Multi-module, PostgreSQL, Spring Batch, Nginx, Kubernetes, Jenkins",
            "https://github.com/2kijung/openclaw-msa",
            null,
            true, 3);
    }

    private void save(String title, String desc, String tech,
                      String github, String live, boolean featured, int order) {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(desc);
        p.setTechnologies(tech);
        p.setGithubUrl(github);
        p.setLiveUrl(live);
        p.setFeatured(featured);
        p.setStatus(Project.Status.DEVELOPMENT);
        p.setDisplayOrder(order);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        projectRepo.save(p);
    }
}
