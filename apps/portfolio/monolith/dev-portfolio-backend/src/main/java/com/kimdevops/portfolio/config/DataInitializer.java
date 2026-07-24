package com.kimdevops.portfolio.config;

import com.kimdevops.portfolio.entity.*;
import com.kimdevops.portfolio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private DevNoteRepository devNoteRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@portfolio.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(User.Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }

        // Create sample projects
        if (projectRepository.count() == 0) {
            createSampleProjects();
        }

        // Create sample contacts
        if (contactRepository.count() == 0) {
            createSampleContacts();
        }

        // Create sample visitors
        if (visitorRepository.count() == 0) {
            createSampleVisitors();
        }

        // Create sample profile (기본정보)
        if (profileRepository.count() == 0) {
            createSampleProfile();
        }

        // Create sample certifications (자격증)
        if (certificationRepository.count() == 0) {
            createSampleCertifications();
        }

        // Create sample careers (경력)
        if (careerRepository.count() == 0) {
            createSampleCareers();
        }

        // Create sample dev notes (개발 노트 - 실제 트러블슈팅 기록)
        if (devNoteRepository.count() == 0) {
            createSampleDevNotes();
        }

        // Create sample skills (기술 스택)
        if (skillRepository.count() == 0) {
            createSampleSkills();
        }

        // Polymer 설명 마이그레이션 (description 컬럼 추가 후 첫 실행 시 1회 적용)
        skillRepository.findAll().stream()
            .filter(s -> s.getName().startsWith("Polymer") && (s.getDescription() == null || s.getDescription().isEmpty()))
            .findFirst()
            .ifPresent(s -> {
                s.setDescription("Google이 YouTube 재설계(2017)에 적용한 JS 웹 컴포넌트 프레임워크. React 이전 Google의 표준 UI 라이브러리.");
                skillRepository.save(s);
            });

        // DevNote 11 openclaw-msa 반영 마이그레이션
        devNoteRepository.findAll().stream()
            .filter(n -> n.getDisplayOrder() == 11 && n.getTitle().startsWith("왜 MSA 대신"))
            .findFirst()
            .ifPresent(n -> {
                n.setTitle("포트폴리오는 모놀리식, openclaw-msa로 MSA 직접 구현 — 두 아키텍처의 선택 근거");
                n.setSituation(
                    "이 포트폴리오 사이트는 Spring Boot 모놀리식으로 설계했고, openclaw-msa(가계부·블로그 자동화 시스템)는 Spring Framework 기반 Maven 멀티모듈 MSA로 별도 구현했다. " +
                    "두 프로젝트에서 같은 개발자가 다른 아키텍처 판단을 내린 이유가 있다.");
                n.setCodeBefore(
                    "// 포트폴리오: 도메인을 패키지로 분리했지만 단일 배포 단위\n" +
                    "// openclaw-msa 초기 구상: 서비스 간 Java 클래스 직접 import\n" +
                    "// → budget-service에서 TelegramNotificationService 직접 참조 시도\n" +
                    "// → package com.openclaw.notification does not exist (컴파일 에러)");
                n.setCodeAfter(
                    "// openclaw-msa 최종 구조: REST 호출만 허용\n" +
                    "// budget-service/NotificationClient.java → POST notification-service:8083/notify/send\n" +
                    "// 컴파일러가 MSA 경계를 강제 — Java import 불가능 구조");
                n.setSolution(
                    "【포트폴리오를 모놀리식으로 선택한 이유】\n" +
                    "개인 포트폴리오 규모에서 MSA를 처음부터 적용하면 서비스 분리·통신·배포 복잡도가 학습 목적을 흐린다. " +
                    "대신 각 도메인(인증/프로필/경력/프로젝트)을 Controller-Service-Repository-Entity 4계층으로 명확히 분리해 두어, " +
                    "추후 특정 도메인만 서비스로 뽑아낼 수 있게 설계했다.\n\n" +
                    "【openclaw-msa를 MSA로 선택한 이유】\n" +
                    "가계부(Spring Batch 메일 파싱)·블로그 자동포스팅·Telegram 알림이 서로 독립적인 배포 주기를 가진다. " +
                    "3개 서비스가 공통 로그인을 공유해야 하는 문제 → JWT를 K8s Secret으로 공유, Nginx auth_request로 단일 인증 관문 구성.\n\n" +
                    "【핵심 교훈】\n" +
                    "Spring Framework(Boot 아님) 순수 설정으로 DispatcherServlet·DataSource·EntityManagerFactory를 직접 정의하면서 " +
                    "프레임워크 내부 동작을 이해했다. MSA 경계는 코드 컨벤션이 아닌 Maven 멀티모듈 구조로 강제했고, " +
                    "서비스 간 Java import 시도는 컴파일 에러로 즉시 차단됐다 — 아키텍처가 빌드 시스템에 의해 물리적으로 보장됨.");
                devNoteRepository.save(n);
            });

        // openclaw-msa 프로젝트 마이그레이션 (기존 DB에 없는 경우 1회 삽입)
        boolean openclawExists = projectRepository.findAll().stream()
            .anyMatch(p -> p.getTitle().startsWith("openclaw-msa"));
        if (!openclawExists) {
            Project openclaw = new Project();
            openclaw.setTitle("openclaw-msa — 가계부·블로그 자동화 MSA 시스템");
            openclaw.setDescription(
                "Python 스크립트로 운영하던 가계부 자동화·블로그 자동포스팅을 Java 엔터프라이즈 아키텍처로 재설계.\n" +
                "Spring Boot 없이 Spring Framework 5.3 순수 설정(DispatcherServlet, DataSource, EntityManagerFactory 직접 정의)으로 구현.\n" +
                "Maven 멀티모듈(budget-service / blog-service / notification-service + openclaw-common BOM) 구조.\n" +
                "단일 로그인→JWT 발급→Nginx auth_request로 전 서비스 인증 통합.\n" +
                "Spring Batch로 메일 파싱 후 가계부 자동 집계, Telegram 알림 발송.");
            openclaw.setTechnologies(
                "Spring Framework 5.3, Maven Multi-module, PostgreSQL, JWT, Nginx API Gateway, Spring Batch, Thymeleaf, Kubernetes, Docker");
            openclaw.setStatus(Project.Status.DEVELOPMENT);
            openclaw.setGithubUrl("https://github.com/2kijung/openclaw-msa");
            openclaw.setFeatured(true);
            openclaw.setDisplayOrder((int)(projectRepository.count() + 1));
            openclaw.setCreatedAt(LocalDateTime.now());
            projectRepository.save(openclaw);
        }
    }

    private void createSampleSkills() {
        // {category, name, level, color}  (level은 칩 UI에선 표시 안 함)
        Object[][] data = {
            {"Backend", "Java", 85, "#f97316"},
            {"Backend", "Spring Framework", 85, "#6db33f"},
            {"Backend", "Spring Boot", 82, "#6db33f"},
            {"Backend", "JPA", 80, "#59666c"},
            {"Backend", "MyBatis", 82, "#8a6d3b"},
            {"Database", "Oracle", 82, "#f80000"},
            {"Database", "MSSQL", 78, "#a91d22"},
            {"Database", "PostgreSQL", 75, "#336791"},
            {"Database", "H2", 72, "#274b6d"},
            {"Frontend", "React", 72, "#61dafb"},
            {"Frontend", "TypeScript", 70, "#3178c6"},
            {"Frontend", "JavaScript", 75, "#f7df1e"},
            {"Frontend", "Polymer (Web Components)", 72, "#ff6d00"},
            {"Frontend", "HTML/CSS", 78, "#e34f26"},
            {"DevOps", "Kubernetes", 75, "#326ce5"},
            {"DevOps", "Docker", 75, "#2496ed"},
            {"DevOps", "Jenkins", 75, "#d33833"},
            {"DevOps", "Git", 82, "#f05032"},
            {"DevOps", "Maven", 78, "#c71a36"},
            {"DevOps", "Jira", 75, "#0052cc"},
        };
        int order = 1;
        for (Object[] d : data) {
            Skill s = new Skill();
            s.setCategory((String) d[0]);
            s.setName((String) d[1]);
            s.setLevel((Integer) d[2]);
            s.setColor((String) d[3]);
            s.setDisplayOrder(order++);
            skillRepository.save(s);
        }
    }

    private DevNote note(int order, String title, String category, String situation,
                         String codeBefore, String codeAfter, String solution) {
        DevNote n = new DevNote();
        n.setDisplayOrder(order);
        n.setTitle(title);
        n.setCategory(category);
        n.setSituation(situation);
        n.setCodeBefore(codeBefore);
        n.setCodeAfter(codeAfter);
        n.setSolution(solution);
        return n;
    }

    private void createSampleDevNotes() {
        // ===== 실무(엠로/SI) 트러블슈팅 사례 =====

        devNoteRepository.save(note(1,
            "SGI 전자보증 송신 3주 장애 — egress→SGI 포트80 미허용", "트러블슈팅",
            "개발계에서 SGI로 정보통보서(INF) 송신 시 respondString is null (STD.EDO3067, 연결 안 됨), 약 30초 timeout. KII·KBI 양사 동일 증상.",
            null, null,
            "원인을 계층별로 벗겨냄: (1)코드/인증서 배제-전자서명 성공·conf 정상 (2)curl로 대상 118.216.173.132:80 i/o timeout 확인(Azure egress 투명 프록시) (3)egress 공인IP가 '랜덤 10개→고정 1개(4.217.240.28)'로 변경되며 SGI 화이트리스트 미등록 (4)최종적으로 SGI 방화벽이 포트80만 미허용(443은 열려 IP 도달은 됨). "
            + "해결: ss로 SYN-SENT(SYN 정상 송출·무응답) 확인 + 443 OPEN/80 FAIL 대조로 차단지점을 'SGI 인바운드 포트80'으로 특정 → 보안팀·인프라로 우리 구간 결백 입증 후 SGI에 포트80 화이트리스트 등록 요청 → 등록 후 curl 200/0.05초, INF 송신 정상 처리(end-to-end 복구). "
            + "기술포인트: dial timeout(무응답 drop) vs RST(refused) 구분으로 방화벽 DROP 판별, 443성공·80실패 비대칭으로 'IP는 도달, 포트만 차단'을 직접 증명."));

        devNoteRepository.save(note(2,
            "SGI 보증서 수신 403 — URL 점(.) vs 슬래시(/)", "트러블슈팅",
            "SGI가 보증서(GUA)를 우리 시스템으로 POST하는데 수신 이력 0건. 한 달간 원인 미상.",
            "POST /sgic.recv.do   -> 403   (SGI가 호출한 잘못된 경로)",
            "POST /sgic/recv.do   -> 200   (정상 경로)",
            "원인: Apache access log에서 SGI가 /sgic.recv.do(점)로 호출 → Spring Security CSRF 예외목록엔 슬래시 경로만 등록돼 있어 점 경로는 외부 POST로 간주(CSRF 토큰 없음) → 403. "
            + "해결: 동일 시각 로그에 우리 curl(슬래시)=200 / SGI(점)=403을 나란히 제시해 경로 오타가 SGI 송신 config에 있음을 입증 → SGI가 등록 URL을 슬래시로 정정 → 수신 성공(EGURDOC 저장·GUR 상태 반영 확인). "
            + "기술포인트: CSRF 필터 로그로 원인을 상대 config로 좁혀 반박 불가한 근거 제시."));

        devNoteRepository.save(note(3,
            "전 화면 그리드 렌더링 불능 — sc-grid 상용 라이선스 만료", "트러블슈팅",
            "어느 날 모든 화면의 그리드가 안 뜨고 콘솔에 `_groupedProvider.grouped is not a function` 발생.",
            null, null,
            "원인: sc-grid(DataLudi) 상용 라이선스 파일(smart-datagrid-lic.js) 만료 → 만료 시 내부 메서드가 무력화되며 해당 에러 발생. "
            + "해결: jar(sc-grid-5.1.2.32) 번들 라이선스로 교체(dev/prod + artifact 2곳, md5 대조). (별개로 검색조건 DuplicateDefinitionError는 cacheBust 불일치 → 동기화.) "
            + "기술포인트: '전역 동시 장애 + 특정 라이브러리 함수 undefined' = 라이선스/버전 만료 1순위 의심 패턴."));

        devNoteRepository.save(note(4,
            "DEV 전 화면 백지 — 프록시의 strict CSP 중복 주입", "트러블슈팅",
            "DEV에서만 로그인창·모든 화면이 안 뜨고 콘솔에 CSP 위반(script/style/font 'self', unsafe-eval 차단) 폭발.",
            "curl -sI (프록시 경유)   -> Content-Security-Policy 헤더 2개 (WAS 관대 + 프록시 strict)",
            "curl -sI (WAS 8010 직접) -> Content-Security-Policy 헤더 1개 (관대)",
            "원인: 앞단 프록시가 응답에 strict CSP를 추가 주입 → WAS의 관대한 CSP와 합쳐져 헤더 2개 → 브라우저가 교집합(가장 엄격) 적용 → Polymer/웹컴포넌트 전멸. "
            + "해결: WAS 직접 vs 프록시 경유 헤더 비교로 범인을 프록시로 특정 → 인프라가 프록시 CSP 주입 제거 → 헤더 1개로 복구. "
            + "기술포인트: 앱 코드는 무결하고 프록시 계층이 헤더를 오염시킨 케이스. 계층별 헤더 비교로 원인 분리."));

        devNoteRepository.save(note(5,
            "보안패치 재부팅 후 서비스 전면 다운 (+Jenkins 빌드 실패)", "트러블슈팅",
            "보안패치 재부팅 후 사이트 접속 불가 + Jenkins 빌드가 git fetch 단계에서 실패.",
            null, null,
            "원인: 자동시작 미설정 서비스들이 재부팅과 함께 내려감(톰캣·앞단 프록시·Gitea). 특히 Gitea가 rootless podman + linger 미설정이라 재부팅 시 종료 → `:3000 Connection refused` → Jenkins git fetch 실패. "
            + "해결: 톰캣/프록시/Gitea 재기동. 재발방지: 톰캣 systemd enable, Gitea loginctl enable-linger + restart=always, 프록시 부팅 자동시작. "
            + "기술포인트: '빌드 실패'의 근본원인이 CI가 아니라 소스저장소(Gitea) 컨테이너 다운 — 빌드→fetch→저장소 포트→컨테이너 상태로 계층 추적."));

        devNoteRepository.save(note(6,
            "Controller 권한검사 우회 — 클래스명 substring 매칭 함정", "트러블슈팅",
            "특정 화면(상담일지)의 .do 호출이 권한검사(role) 없이 통과되는 보안 취약점 발견.",
            """
            // 화이트리스트를 클래스명 substring contains 로 매칭
            "MeetingLogController".contains("LogController") == true  // → 권한검사 bypass
            """,
            """
            // 네이밍 충돌 제거 (정확 매칭 필요)
            MeetingLogController  ->  MeetingController
            """,
            "원인: AuthCheckInterceptor가 예외목록(LogController 등)을 클래스명 substring contains로 매칭 → MeetingLogController에 'LogController'가 포함되어 권한검사가 통째로 우회됨. "
            + "해결: Controller명을 MeetingController로 변경(substring 충돌 제거). 진단 시그널: 해당 .do에 role.selectRoleWithUrl 쿼리가 안 돌면 bypass 의심. "
            + "기술포인트: 문자열 포함(contains) 기반 화이트리스트가 네이밍 충돌로 보안 구멍을 만든 사례 → 정확 매칭/네이밍 규칙 필요."));

        devNoteRepository.save(note(7,
            "이기종 DB 이관 — Oracle SQL 직역 실패 (→ PostgreSQL)", "트러블슈팅",
            "AS-IS(Oracle) SQL을 TO-BE(PostgreSQL)로 이관 시 컴파일 에러·미존재 테이블 참조가 반복됨.",
            """
            DECODE(...)                 -- Oracle
            NVL(a, b)                   -- Oracle
            TEN_ID / BUKRS / REG_USR_ID
            EINA, PLT (미존재 테이블 참조)
            ESHMKUSE (오타)
            """,
            """
            CASE WHEN ... END           -- PostgreSQL
            COALESCE(a, b)              -- PostgreSQL
            SYS_ID / COMP_CD / REGR_ID
            ORG WHERE org_typ_cd='PLANT' (대체)
            ESHKUSE (정정)
            """,
            "원인: Oracle 전용 함수/컬럼/테이블을 PG로 직역하며 미존재 객체를 참조. "
            + "해결: 컬럼 매핑(TEN_ID↔SYS_ID, BUKRS↔COMP_CD, REG_USR_ID↔REGR_ID), 함수 변환(DECODE→CASE, NVL→COALESCE), 미존재 테이블 대체(EINA·PLT→ORG org_typ_cd='PLANT'), 오타 정정(ESHMKUSE→ESHKUSE). "
            + "재발방지: SQL 작성 전 PG 실 스키마(information_schema) 검증을 표준화해 '직역 금지'. 기술포인트: 이기종 DB 이관은 문법 변환이 아니라 실 스키마 대조가 우선."));

        // ===== 이 포트폴리오 프로젝트 트러블슈팅 =====
        devNoteRepository.save(note(8,
            "CORS 에러: allowCredentials와 와일드카드 충돌", "트러블슈팅",
            "프론트(localhost:3000)에서 백엔드(localhost:8081) API를 호출하니 모든 요청이 500 에러로 실패했다. 서버 로그에 IllegalArgumentException이 반복 출력됐다.",
            """
            configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:3000", "http://localhost:5173", "*"));
            configuration.setAllowCredentials(true);
            """,
            """
            // "*" 대신 allowedOriginPatterns 사용 → 와일드카드 + 인증정보 병행 가능
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
            configuration.setAllowCredentials(true);
            """,
            "원인: allowCredentials(true)일 때 setAllowedOrigins에 \"*\"를 넣으면 Spring이 예외를 던진다(자격증명 허용 시 Access-Control-Allow-Origin에 * 사용 불가). 해결: setAllowedOriginPatterns로 교체하니 preflight(OPTIONS) 200 + 정상 응답. 교훈: 보안 관련 설정은 브라우저 CORS 규칙과 함께 이해해야 한다."));

        // 2) Spring Security 기본 보안
        devNoteRepository.save(note(9,
            "로그인이 빈 401로 막힘: SecurityFilterChain 누락", "트러블슈팅",
            "로그인·조회 요청이 빈 응답과 함께 401로 막혔다. 프론트 콘솔엔 'Unexpected end of JSON input', 서버 로그엔 'Using generated security password'가 떴다.",
            """
            @Configuration
            public class SecurityConfig {
                @Bean
                public PasswordEncoder passwordEncoder() { ... }
                @Bean
                public CorsConfigurationSource corsConfigurationSource() { ... }
                // SecurityFilterChain 빈이 없음 → 기본 보안이 모든 요청 차단
            }
            """,
            """
            @Bean
            public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                    .cors(Customizer.withDefaults())
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
                return http.build();
            }
            """,
            "원인: SecurityFilterChain 빈이 없으면 Spring Security 기본 설정이 활성화되어 모든 요청이 401로 차단된다('Using generated security password'가 그 신호). 해결: 필터체인 빈을 추가해 CSRF 비활성 + CORS 적용 + 요청 허용. (JWT 강제 검증은 추후 필터로 확장 예정)"));

        // 3) H2 영구저장 (기술 선택)
        devNoteRepository.save(note(10,
            "데이터가 재시작마다 사라짐: 인메모리 → 파일 DB", "기술선택",
            "관리자에서 정보를 수정해도 백엔드를 재시작하면 초기값으로 돌아갔다. 개발 중 데이터가 계속 초기화되어 불편했다.",
            """
            spring:
              datasource:
                url: jdbc:h2:mem:portfolio_db      # 인메모리
              jpa:
                hibernate:
                  ddl-auto: create-drop            # 종료 시 테이블 삭제
            """,
            """
            spring:
              datasource:
                url: jdbc:h2:file:./data/portfolio;AUTO_SERVER=TRUE  # 파일 저장
              jpa:
                hibernate:
                  ddl-auto: update                 # 스키마/데이터 보존
            """,
            "원인: H2 인메모리 + create-drop은 프로세스 종료 시 데이터가 사라진다(개발 편의용). 해결: 파일 기반 H2 + ddl-auto=update로 전환해 재시작해도 데이터 유지. AUTO_SERVER=TRUE로 외부 DB툴 접속도 가능. 실무 배포 시엔 JPA 덕분에 URL만 바꿔 PostgreSQL로 교체 가능(엔티티/코드 불변)."));

        // 4) 아키텍처 설계 (모놀리식 선택)
        devNoteRepository.save(note(11,
            "포트폴리오는 모놀리식, openclaw-msa로 MSA 직접 구현 — 두 아키텍처의 선택 근거", "설계",
            "이 포트폴리오 사이트는 Spring Boot 모놀리식으로 설계했고, openclaw-msa(가계부·블로그 자동화 시스템)는 Spring Framework 기반 Maven 멀티모듈 MSA로 별도 구현했다. " +
            "두 프로젝트에서 같은 개발자가 다른 아키텍처 판단을 내린 이유가 있다.",
            "// 포트폴리오: 도메인을 패키지로 분리했지만 단일 배포 단위\n" +
            "// openclaw-msa 초기 구상: 서비스 간 Java 클래스 직접 import\n" +
            "// → budget-service에서 TelegramNotificationService 직접 참조 시도\n" +
            "// → package com.openclaw.notification does not exist (컴파일 에러)",
            "// openclaw-msa 최종 구조: REST 호출만 허용\n" +
            "// budget-service/NotificationClient.java → POST notification-service:8083/notify/send\n" +
            "// 컴파일러가 MSA 경계를 강제 — Java import 불가능 구조",
            "【포트폴리오를 모놀리식으로 선택한 이유】\n" +
            "개인 포트폴리오 규모에서 MSA를 처음부터 적용하면 서비스 분리·통신·배포 복잡도가 학습 목적을 흐린다. " +
            "대신 각 도메인(인증/프로필/경력/프로젝트)을 Controller-Service-Repository-Entity 4계층으로 명확히 분리해 두어, " +
            "추후 특정 도메인만 서비스로 뽑아낼 수 있게 설계했다.\n\n" +
            "【openclaw-msa를 MSA로 선택한 이유】\n" +
            "가계부(Spring Batch 메일 파싱)·블로그 자동포스팅·Telegram 알림이 서로 독립적인 배포 주기를 가진다. " +
            "3개 서비스가 공통 로그인을 공유해야 하는 문제 → JWT를 K8s Secret으로 공유, Nginx auth_request로 단일 인증 관문 구성.\n\n" +
            "【핵심 교훈】\n" +
            "Spring Framework(Boot 아님) 순수 설정으로 DispatcherServlet·DataSource·EntityManagerFactory를 직접 정의하면서 " +
            "프레임워크 내부 동작을 이해했다. MSA 경계는 코드 컨벤션이 아닌 Maven 멀티모듈 구조로 강제했고, " +
            "서비스 간 Java import 시도는 컴파일 에러로 즉시 차단됐다 — 아키텍처가 빌드 시스템에 의해 물리적으로 보장됨."));
    }

    private void createSampleCareers() {
        Career c = new Career();
        c.setCompany("엠로 (Emro)");
        c.setPosition("Java Backend Developer");
        c.setStartDate("2022-02");
        c.setEndDate("");   // 재직 중
        c.setDescription("국내 구매(SRM) 솔루션 전문기업. 은행·공기업·대기업 고객사 SI 프로젝트에서 "
                + "인터페이스/API·배치 개발, 레거시 시스템 고도화, 외부 시스템 연계 개발을 수행. "
                + "Maven·Jira 기반 협업 및 다국어 시스템 개발 경험.");
        c.setDisplayOrder(1);
        careerRepository.save(c);
    }

    private void createSampleCertifications() {
        String[][] certs = {
            // {name, issuer, acquiredDate, score}
            {"정보처리기사", "한국산업인력공단", "2023-05", ""},
            {"SQLD (SQL 개발자)", "한국데이터산업진흥원", "2023-09", ""},
            {"리눅스마스터 2급", "KAIT", "2022-11", "2급"},
        };
        int order = 1;
        for (String[] c : certs) {
            Certification cert = new Certification();
            cert.setName(c[0]);
            cert.setIssuer(c[1]);
            cert.setAcquiredDate(c[2]);
            cert.setScore(c[3]);
            cert.setDisplayOrder(order++);
            certificationRepository.save(cert);
        }
    }

    private void createSampleProfile() {
        Profile profile = new Profile();
        profile.setName("이기정");
        profile.setBirthDate("");        // 관리자에서 입력
        profile.setLocation("");
        profile.setUniversity("");
        profile.setMajor("");
        profile.setGraduationStatus("");
        profile.setMilitaryStatus("");
        profile.setIntroduction("비즈니스 요구사항을 안정적인 서비스로 구현하는 것을 좋아하는 Java 백엔드 개발자입니다. 은행·공기업·대기업 SI 프로젝트에서 Spring Framework·MyBatis 기반의 인터페이스/API·배치·연계 개발과 레거시 시스템 고도화를 수행했습니다. 최신 스택(Spring Boot·JPA·React)은 이 포트폴리오를 직접 설계·구현하며 학습하고 있습니다.");
        profile.setEmail("dlrlwjd1313@naver.com");
        profile.setGithubUrl("https://github.com/2kijung");
        profile.setTistoryUrl("https://dlrlwjd1313.tistory.com");
        profile.setBlogUrl("https://velog.io/@dlrlwjd1313/posts");
        profile.setImageUrl("");         // 사진은 관리자에서 업로드
        profile.setCurrentStatus("KOLON에서 SGI보증보험 연계와 AI 기능 연계·개발을 진행하고 있습니다.");
        profile.setCreatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }

    private void createSampleProjects() {
        String[][] projectsData = {
            {"KOLON — SGI보증보험 연계 & AI 기능 개발",
             "SGI서울보증 보증보험 시스템 연계 개발 및 AI 기능 연계·개발 진행. (현재 진행 중)",
             "Java, Spring Framework, MyBatis, Oracle, REST API, AI 연계", "DEVELOPMENT"},
            {"현대ITC — 구매/인터페이스 전반 담당 & 2차 단독 고도화",
             "구매·인터페이스 업무 전반을 단독으로 담당·컨트롤. 1차 프로젝트 철수 후 2차에서 고도화 업무를 혼자 맡아 요구사항 분석부터 개발·안정화까지 end-to-end로 수행 (오너십/단독 수행).",
             "Java, Spring, MyBatis, Oracle, Interface/API", "PRODUCTION"},
            {"한국지역난방공사 — 구매시스템 고도화",
             "판교 상주(8개월). 구형 시스템 고도화 및 기존 솔루션 소스 개선. 공기업 수준의 감사·보안 제약(웹 취약점·접근성) 대응 경험.",
             "Java, Spring, MyBatis, Oracle", "PRODUCTION"},
            {"원익IPS — 구매시스템 인터페이스 API 개발",
             "실제 구매 업무에서 사용되는 인터페이스 API 개발 및 고객사 요구사항 대응.",
             "Java, Spring Framework, MyBatis, Oracle, REST API", "PRODUCTION"},
            {"우리은행 — 웹 취약점·접근성 & 연계 개발",
             "웹 취약점·웹 접근성 진단 및 조치. LX판토스 연계 배치(Java 스케줄러) 개발, 카카오톡 알림 연계 개발.",
             "Java, Spring, Batch/Scheduler, MSSQL", "PRODUCTION"},
            {"IBK — 리포팅 출력물 개발",
             "리포팅 툴 기반 출력물 산출 기능 개발.",
             "Java, Spring, 리포팅 툴, Oracle", "PRODUCTION"},
            {"엠로 본사 솔루션 — 핵심 기능 개발",
             "구매(SRM) 솔루션 핵심 기능 개발. Maven·Jira 기반 협업 및 다국어 시스템 대응.",
             "Java, Spring Framework, MyBatis, Maven, Jira", "PRODUCTION"},
            {"openclaw-msa — 가계부·블로그 자동화 MSA 시스템",
             "Python 스크립트로 운영하던 가계부 자동화·블로그 자동포스팅을 Java 엔터프라이즈 아키텍처로 재설계.\n" +
             "Spring Boot 없이 Spring Framework 5.3 순수 설정(DispatcherServlet, DataSource, EntityManagerFactory 직접 정의)으로 구현.\n" +
             "Maven 멀티모듈(budget-service / blog-service / notification-service + openclaw-common BOM) 구조.\n" +
             "단일 로그인→JWT 발급→Nginx auth_request로 전 서비스 인증 통합.\n" +
             "Spring Batch로 메일 파싱 후 가계부 자동 집계, Telegram 알림 발송.",
             "Spring Framework 5.3, Maven Multi-module, PostgreSQL, JWT, Nginx API Gateway, Spring Batch, Thymeleaf, Kubernetes, Docker",
             "DEVELOPMENT"},
        };

        for (String[] data : projectsData) {
            Project project = new Project();
            project.setTitle(data[0]);
            project.setDescription(data[1]);
            project.setTechnologies(data[2]);
            project.setStatus(Project.Status.valueOf(data[3]));
            project.setFeatured(true);
            project.setDisplayOrder((int)(projectRepository.count() + 1));
            project.setCreatedAt(LocalDateTime.now());
            projectRepository.save(project);
        }
    }

    private void createSampleContacts() {
        String[][] contactsData = {
            {"John Doe", "john@example.com", "Project Inquiry", "Interested in your microservices expertise"},
            {"Jane Smith", "jane@example.com", "Collaboration", "Let's work together on a Kubernetes project"},
            {"Tech Lead", "tech@company.com", "Consulting", "Need help with Spring Boot migration"},
        };

        for (String[] data : contactsData) {
            Contact contact = new Contact();
            contact.setName(data[0]);
            contact.setEmail(data[1]);
            contact.setSubject(data[2]);
            contact.setMessage(data[3]);
            contact.setStatus(Contact.Status.NEW);
            contact.setCreatedAt(LocalDateTime.now());
            contactRepository.save(contact);
        }
    }

    private void createSampleVisitors() {
        for (int i = 0; i < 50; i++) {
            Visitor visitor = new Visitor();
            visitor.setIpAddress("192.168.1." + (i % 256));
            visitor.setPage(i % 2 == 0 ? "/" : "/projects");
            visitor.setSessionId("session-" + i);
            visitor.setVisitedAt(LocalDateTime.now().minusHours(i));
            visitorRepository.save(visitor);
        }
    }
}
