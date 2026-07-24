# 풀스택 포트폴리오 - 기술 스택 상세 분석

## 📋 목차
1. [전체 아키텍처](#전체-아키텍처)
2. [프론트엔드 기술 스택](#프론트엔드-기술-스택)
3. [백엔드 기술 스택](#백엔드-기술-스택)
4. [배포 & 인프라](#배포--인프라)
5. [데이터 흐름](#데이터-흐름)
6. [주요 기술 선택 이유](#주요-기술-선택-이유)

---

## 🏗️ 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     클라이언트 (브라우저)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              React + TypeScript SPA (포트 5173)              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Pages: Home, AdminDashboard, LoginPage             │  │
│  │  Components: Navbar, HeroSection, SkillsSection     │  │
│  │  Services: API 통신 레이어 (lib/api.ts)             │  │
│  │  State: React Hooks + Context API                   │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API + JWT Token
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         Spring Boot 3.x Backend (포트 8080)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controllers: Auth, Contact, Project, Visitor       │  │
│  │  Services: 비즈니스 로직 처리                        │  │
│  │  Repositories: JPA 데이터 접근                       │  │
│  │  Security: JWT 인증, Spring Security                │  │
│  │  Config: CORS, DataInitializer                      │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ SQL
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              데이터베이스 (H2 또는 PostgreSQL)                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tables: users, projects, contacts, visitors        │  │
│  │  Relationships: User(1) → Projects(N)               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 프론트엔드 기술 스택

### 1. React 19 + TypeScript

**선택 이유:**
- 최신 React 19의 자동 배치 최적화
- TypeScript로 타입 안정성 확보
- 컴포넌트 기반 아키텍처로 재사용성 극대화

**주요 파일 구조:**
```
client/src/
├── pages/
│   ├── Home.tsx              # 포트폴리오 메인 페이지
│   ├── AdminDashboard.tsx    # 관리자 대시보드
│   └── LoginPage.tsx         # 로그인 페이지
├── components/
│   ├── Navbar.tsx            # 네비게이션 바
│   ├── ParticleBackground.tsx # 파티클 배경 효과
│   └── sections/             # 각 섹션 컴포넌트들
├── hooks/
│   ├── useInView.ts          # 스크롤 감지 훅
│   └── useTypewriter.ts      # 타이핑 애니메이션 훅
├── lib/
│   └── api.ts                # API 통신 레이어
├── contexts/
│   └── ThemeContext.tsx      # 테마 관리
└── App.tsx                   # 라우팅 설정
```

**핵심 코드 예시:**

```typescript
// lib/api.ts - API 통신 레이어
export const authApi = {
  login: async (username: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    const data = await response.json();
    if (data.success && data.data?.token) {
      localStorage.setItem('token', data.data.token);
    }
    return data;
  },
};

// pages/AdminDashboard.tsx - 관리자 대시보드
export default function AdminDashboard() {
  const [contacts, setContacts] = useState<any[]>([]);
  
  useEffect(() => {
    const loadData = async () => {
      const res = await contactApi.getAll();
      if (res.success) setContacts(res.data || []);
    };
    loadData();
  }, []);
  
  return (
    <div className="min-h-screen bg-background">
      {/* 대시보드 UI */}
    </div>
  );
}
```

### 2. Vite

**선택 이유:**
- 초고속 개발 서버 (HMR - Hot Module Replacement)
- 번들 크기 최소화
- ES 모듈 기반 네이티브 지원

**설정:**
```javascript
// vite.config.ts
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default {
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
}
```

### 3. Tailwind CSS 4 + shadcn/ui

**선택 이유:**
- 유틸리티 기반 CSS로 빠른 개발
- 커스터마이징 가능한 디자인 시스템
- shadcn/ui로 고급 컴포넌트 활용

**주요 컴포넌트:**
```typescript
// Button, Card, Tabs, Dialog 등 50+ 컴포넌트 사용
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
```

**글로벌 스타일 설정:**
```css
/* index.css - OKLCH 색상 시스템 */
:root {
  --primary: oklch(0.623 0.214 259.815);
  --background: oklch(0.141 0.005 285.823);
  --foreground: oklch(0.85 0.005 65);
  /* ... 더 많은 색상 변수 */
}
```

### 4. Wouter (클라이언트 라우팅)

**선택 이유:**
- 가볍고 빠른 라우팅 (3KB)
- React Router 대비 번들 크기 50% 감소
- 간단한 API로 빠른 구현

**라우팅 설정:**
```typescript
// App.tsx
function Router() {
  return (
    <Switch>
      <Route path={"/"} component={Home} />
      <Route path={"/login"} component={LoginPage} />
      <Route path={"/admin"} component={AdminDashboard} />
      <Route path={"/404"} component={NotFound} />
      <Route component={NotFound} />
    </Switch>
  );
}
```

### 5. Framer Motion (애니메이션)

**선택 이유:**
- 선언적 애니메이션 API
- 성능 최적화된 GPU 가속
- 복잡한 인터랙션 간단하게 구현

**사용 예시:**
```typescript
// ParticleBackground.tsx - 파티클 애니메이션
import { motion } from 'framer-motion';

export default function ParticleBackground() {
  return (
    <motion.div
      animate={{ opacity: [0.3, 0.8, 0.3] }}
      transition={{ duration: 3, repeat: Infinity }}
      className="particle"
    />
  );
}
```

### 6. Lucide React (아이콘)

**선택 이유:**
- 450+ 고품질 SVG 아이콘
- Tree-shaking 지원으로 번들 최소화
- 일관된 디자인

```typescript
import { Mail, Code, BarChart3, LogOut } from 'lucide-react';
```

---

## 🔧 백엔드 기술 스택

### 1. Spring Boot 3.2 + JDK 17

**선택 이유:**
- JDK 17의 최신 기능 (Records, Sealed Classes)
- Spring Boot 3.x의 성능 개선
- 장기 지원(LTS) 버전으로 안정성 보장

**메인 애플리케이션:**
```java
// PortfolioBackendApplication.java
@SpringBootApplication
public class PortfolioBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioBackendApplication.class, args);
    }
}
```

**애플리케이션 설정:**
```yaml
# application.yml
spring:
  application:
    name: portfolio-backend
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  security:
    user:
      name: admin
      password: admin
```

### 2. Spring Data JPA + Hibernate

**선택 이유:**
- ORM으로 SQL 작성 최소화
- 자동 쿼리 생성
- 관계 매핑 자동화

**엔티티 설계:**
```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum Role {
        ADMIN, USER
    }
}

// Project.java
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String technologies;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private Boolean featured;
    
    private Integer displayOrder;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum Status {
        DEVELOPMENT, PRODUCTION, ARCHIVED
    }
}

// Contact.java
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum Status {
        NEW, REVIEWED, RESPONDED, ARCHIVED
    }
}

// Visitor.java
@Entity
@Table(name = "visitors")
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ipAddress;
    private String page;
    private String sessionId;
    private String userAgent;
    
    @CreationTimestamp
    private LocalDateTime visitedAt;
}
```

**Repository 정의:**
```java
// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}

// ProjectRepository.java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderByDisplayOrderAsc(Project.Status status);
    List<Project> findByFeaturedTrue();
}

// ContactRepository.java
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Page<Contact> findByStatusOrderByCreatedAtDesc(Contact.Status status, Pageable pageable);
}

// VisitorRepository.java
@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    long countByVisitedAtAfter(LocalDateTime dateTime);
    long countDistinctByIpAddress();
    long countDistinctBySessionId();
}
```

### 3. Spring Security + JWT

**선택 이유:**
- 산업 표준 인증 메커니즘
- 상태 비저장(Stateless) 아키텍처
- 마이크로서비스 친화적

**보안 설정:**
```java
// SecurityConfig.java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

// JwtUtil.java
@Component
public class JwtUtil {
    @Value("${jwt.secret:your-secret-key}")
    private String jwtSecret;
    
    private static final long JWT_EXPIRATION = 86400000; // 24시간
    
    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
    }
    
    public String extractUsername(String token) {
        return Jwts.parser()
            .verifyingKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
    
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyingKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**JWT 토큰 흐름:**
```
1. 클라이언트 로그인
   POST /api/auth/login
   { "username": "admin", "password": "admin" }

2. 서버 검증 및 토큰 생성
   - 비밀번호 BCrypt 검증
   - JWT 토큰 생성 (24시간 유효)

3. 토큰 반환
   { 
     "success": true,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "user": { "id": 1, "username": "admin", "role": "ADMIN" }
     }
   }

4. 클라이언트 저장
   localStorage.setItem('token', token)

5. 이후 요청에 포함
   Authorization: Bearer <token>

6. 서버 검증
   - 토큰 서명 검증
   - 만료 시간 확인
   - 사용자 권한 확인
```

### 4. REST API 설계

**컨트롤러 구현:**
```java
// AuthController.java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(true, response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}

// ProjectController.java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(new ApiResponse<>(true, projects));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO dto) {
        ProjectDTO created = projectService.createProject(dto);
        return ResponseEntity.status(201)
            .body(new ApiResponse<>(true, created));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProject(
        @PathVariable Long id,
        @RequestBody ProjectDTO dto
    ) {
        ProjectDTO updated = projectService.updateProject(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, updated));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null));
    }
}

// ContactController.java
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    
    @Autowired
    private ContactService contactService;
    
    @PostMapping
    public ResponseEntity<?> submitContact(@RequestBody ContactDTO dto) {
        ContactDTO saved = contactService.saveContact(dto);
        return ResponseEntity.status(201)
            .body(new ApiResponse<>(true, saved));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllContacts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<ContactDTO> contacts = contactService.getAllContacts(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, contacts));
    }
}

// VisitorController.java
@RestController
@RequestMapping("/api/visitors")
public class VisitorController {
    
    @Autowired
    private VisitorService visitorService;
    
    @PostMapping
    public ResponseEntity<?> recordVisit(@RequestBody VisitorDTO dto) {
        VisitorDTO saved = visitorService.recordVisit(dto);
        return ResponseEntity.ok(new ApiResponse<>(true, saved));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        VisitorStatsDTO stats = visitorService.getStats();
        return ResponseEntity.ok(new ApiResponse<>(true, stats));
    }
}
```

### 5. 서비스 계층

**비즈니스 로직 구현:**
```java
// AuthService.java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        UserDTO userDTO = toUserDTO(user);
        
        return new LoginResponse(token, userDTO);
    }
    
    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

// ProjectService.java
@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setTechnologies(dto.getTechnologies());
        project.setStatus(Project.Status.valueOf(dto.getStatus()));
        project.setFeatured(dto.isFeatured());
        project.setCreatedAt(LocalDateTime.now());
        
        Project saved = projectRepository.save(project);
        return toDTO(saved);
    }
    
    private ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setTechnologies(project.getTechnologies());
        dto.setStatus(project.getStatus().name());
        dto.setFeatured(project.getFeatured());
        return dto;
    }
}

// VisitorService.java
@Service
public class VisitorService {
    
    @Autowired
    private VisitorRepository visitorRepository;
    
    @Transactional
    public VisitorDTO recordVisit(VisitorDTO dto) {
        Visitor visitor = new Visitor();
        visitor.setIpAddress(dto.getIpAddress());
        visitor.setPage(dto.getPage());
        visitor.setSessionId(dto.getSessionId());
        visitor.setUserAgent(dto.getUserAgent());
        visitor.setVisitedAt(LocalDateTime.now());
        
        Visitor saved = visitorRepository.save(visitor);
        return toDTO(saved);
    }
    
    public VisitorStatsDTO getStats() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        VisitorStatsDTO stats = new VisitorStatsDTO();
        stats.setTotalVisitors(visitorRepository.count());
        stats.setUniqueIPs(visitorRepository.countDistinctByIpAddress());
        stats.setUniqueSessions(visitorRepository.countDistinctBySessionId());
        stats.setPageViews(visitorRepository.countByVisitedAtAfter(last24Hours));
        
        return stats;
    }
    
    private VisitorDTO toDTO(Visitor visitor) {
        VisitorDTO dto = new VisitorDTO();
        dto.setId(visitor.getId());
        dto.setIpAddress(visitor.getIpAddress());
        dto.setPage(visitor.getPage());
        dto.setSessionId(visitor.getSessionId());
        dto.setVisitedAt(visitor.getVisitedAt());
        return dto;
    }
}
```

### 6. 초기 데이터 로더

**DataInitializer:**
```java
// DataInitializer.java
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Admin 사용자 생성
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@portfolio.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(User.Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }
        
        // 샘플 프로젝트 생성
        if (projectRepository.count() == 0) {
            createSampleProjects();
        }
    }
    
    private void createSampleProjects() {
        String[][] projectsData = {
            {"E-Commerce Platform", "Full-stack e-commerce", "Spring Boot, React, Docker", "PRODUCTION"},
            {"Microservices Architecture", "Distributed system", "Spring Cloud, Kubernetes", "PRODUCTION"},
            // ... 더 많은 샘플 데이터
        };
        
        for (String[] data : projectsData) {
            Project project = new Project();
            project.setTitle(data[0]);
            project.setDescription(data[1]);
            project.setTechnologies(data[2]);
            project.setStatus(Project.Status.valueOf(data[3]));
            project.setCreatedAt(LocalDateTime.now());
            projectRepository.save(project);
        }
    }
}
```

---

## 🐳 배포 & 인프라

### 1. Docker

**Dockerfile:**
```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/portfolio-backend-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**이미지 빌드 및 실행:**
```bash
# 이미지 빌드
docker build -t dev-portfolio-backend:latest .

# 컨테이너 실행
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e JWT_SECRET=your-secret \
  --name portfolio-backend \
  dev-portfolio-backend:latest
```

### 2. Docker Compose

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  # PostgreSQL 데이터베이스
  postgres:
    image: postgres:15-alpine
    container_name: portfolio-db
    environment:
      POSTGRES_DB: portfolio_db
      POSTGRES_USER: portfolio_user
      POSTGRES_PASSWORD: portfolio_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U portfolio_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Spring Boot 백엔드
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: portfolio-backend
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: portfolio_db
      DB_USER: portfolio_user
      DB_PASSWORD: portfolio_password
      JWT_SECRET: your-jwt-secret-key
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/auth/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  # React 프론트엔드
  frontend:
    build:
      context: ../dev-portfolio
      dockerfile: Dockerfile
    container_name: portfolio-frontend
    ports:
      - "5173:5173"
    environment:
      VITE_API_URL: http://localhost:8080/api
    depends_on:
      - backend

volumes:
  postgres_data:
```

**실행 명령어:**
```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 서비스 중지
docker-compose down
```

### 3. Kubernetes

**Deployment 매니페스트:**
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: portfolio-backend
  namespace: portfolio
spec:
  replicas: 3
  selector:
    matchLabels:
      app: portfolio-backend
  template:
    metadata:
      labels:
        app: portfolio-backend
    spec:
      containers:
      - name: backend
        image: your-registry/dev-portfolio-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: portfolio-config
              key: DB_HOST
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: portfolio-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: portfolio-secrets
              key: JWT_SECRET
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/auth/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/auth/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: portfolio-backend-service
  namespace: portfolio
spec:
  type: LoadBalancer
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: portfolio-backend
```

**배포 명령어:**
```bash
# Namespace 생성
kubectl create namespace portfolio

# ConfigMap 생성
kubectl create configmap portfolio-config \
  --from-literal=DB_HOST=postgres \
  --from-literal=DB_PORT=5432 \
  -n portfolio

# Secret 생성
kubectl create secret generic portfolio-secrets \
  --from-literal=DB_PASSWORD=your-password \
  --from-literal=JWT_SECRET=your-secret \
  -n portfolio

# 배포
kubectl apply -f k8s/deployment.yaml -n portfolio

# 상태 확인
kubectl get pods -n portfolio
kubectl get svc -n portfolio
```

---

## 📊 데이터 흐름

### 1. 사용자 로그인 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ 1. 클라이언트 (React)                                        │
│    - LoginPage.tsx에서 username/password 입력              │
│    - authApi.login() 호출                                  │
└──────────────────────┬──────────────────────────────────────┘
                       │ POST /api/auth/login
                       │ { username, password }
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. 서버 (Spring Boot)                                       │
│    - AuthController.login() 수신                           │
│    - AuthService.login() 호출                              │
│    - UserRepository에서 사용자 조회                         │
│    - BCrypt로 비밀번호 검증                                 │
│    - JwtUtil.generateToken() 호출                          │
└──────────────────────┬──────────────────────────────────────┘
                       │ { token, user }
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. 클라이언트 저장                                          │
│    - localStorage.setItem('token', token)                 │
│    - 이후 모든 요청에 Authorization 헤더 포함              │
└─────────────────────────────────────────────────────────────┘
```

### 2. 프로젝트 조회 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ 1. 클라이언트 (React)                                        │
│    - Home.tsx에서 projectApi.getAll() 호출                 │
│    - useEffect에서 데이터 로드                              │
└──────────────────────┬──────────────────────────────────────┘
                       │ GET /api/projects
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. 서버 (Spring Boot)                                       │
│    - ProjectController.getAllProjects() 수신               │
│    - ProjectService.getAllProjects() 호출                  │
│    - ProjectRepository.findAll() 실행                      │
│    - JPA가 SQL 생성 및 실행                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ SELECT * FROM projects
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. 데이터베이스 (H2/PostgreSQL)                             │
│    - 모든 프로젝트 반환                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │ [Project1, Project2, ...]
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. 서버 응답                                                │
│    - Project → ProjectDTO 변환                             │
│    - ApiResponse 래핑                                      │
│    - JSON 직렬화                                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ { success: true, data: [...] }
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. 클라이언트 렌더링                                        │
│    - 응답 데이터를 state에 저장                             │
│    - ProjectsSection에서 카드 렌더링                        │
└─────────────────────────────────────────────────────────────┘
```

### 3. 관리자 대시보드 데이터 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ AdminDashboard.tsx                                          │
│ - useEffect에서 3개 API 병렬 호출                           │
│   1. contactApi.getAll()                                   │
│   2. projectApi.getAll()                                   │
│   3. visitorApi.getStats()                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ 3개 API 동시 요청
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 서버 (Spring Boot)                                          │
│ - ContactController.getAllContacts()                       │
│ - ProjectController.getAllProjects()                       │
│ - VisitorController.getStats()                             │
└──────────────────────┬──────────────────────────────────────┘
                       │ 3개 응답 병렬 반환
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 클라이언트 렌더링                                           │
│ - 통계 카드 표시                                            │
│ - Tabs 컴포넌트에서 데이터 표시                             │
│   - Contacts Tab: 연락처 목록                              │
│   - Projects Tab: 프로젝트 목록                            │
│   - Stats Tab: 상세 통계                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 주요 기술 선택 이유

| 기술 | 선택 이유 | 대안 | 비교 |
|------|---------|------|------|
| **React 19** | 최신 기능, 성능 최적화, 커뮤니티 | Vue, Angular | React가 가장 큰 생태계 |
| **TypeScript** | 타입 안정성, 개발 생산성 | JavaScript | 대규모 프로젝트에 필수 |
| **Vite** | 초고속 개발 서버, 작은 번들 | Webpack | Webpack 대비 10배 빠름 |
| **Tailwind CSS** | 유틸리티 기반, 빠른 개발 | CSS-in-JS | 번들 크기 작음 |
| **Spring Boot 3** | 최신 기능, JDK 17 지원 | Quarkus | Spring 생태계 가장 큼 |
| **JPA/Hibernate** | ORM 표준, 생산성 | MyBatis | 복잡한 쿼리는 MyBatis 우수 |
| **JWT** | 상태 비저장, 마이크로서비스 | Session | 분산 시스템에 최적 |
| **Docker** | 표준 컨테이너, 배포 간편 | Podman | 산업 표준 |
| **Kubernetes** | 프로덕션 오케스트레이션 | Docker Swarm | 엔터프라이즈 표준 |
| **H2 Database** | 개발 편의성, 인메모리 | SQLite | Java 기반 프로젝트에 최적 |
| **PostgreSQL** | 강력한 기능, 안정성 | MySQL | 복잡한 쿼리 지원 우수 |

---

## 📈 성능 최적화 전략

### 프론트엔드 최적화
```typescript
// 1. 코드 분할 (Code Splitting)
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));

// 2. 메모이제이션
const MemoizedComponent = memo(MyComponent);

// 3. 이미지 최적화
<img src={imageUrl} alt="description" loading="lazy" />

// 4. 번들 분석
// vite-plugin-visualizer로 번들 크기 분석
```

### 백엔드 최적화
```java
// 1. 쿼리 최적화
@Query("SELECT p FROM Project p WHERE p.featured = true")
List<Project> findFeaturedProjects();

// 2. 캐싱
@Cacheable("projects")
public List<ProjectDTO> getAllProjects() { ... }

// 3. 페이지네이션
Page<Contact> contacts = contactRepository.findAll(PageRequest.of(0, 10));

// 4. 데이터베이스 인덱싱
@Column(unique = true, nullable = false)
private String username;
```

---

## 🔒 보안 고려사항

1. **인증**: JWT 토큰 기반 상태 비저장 인증
2. **암호화**: BCrypt로 비밀번호 해싱
3. **CORS**: 허용된 출처만 API 접근
4. **HTTPS**: 프로덕션에서 필수
5. **환경 변수**: 민감한 정보는 환경 변수로 관리
6. **SQL 인젝션 방지**: JPA 파라미터 바인딩
7. **CSRF 보호**: Spring Security 기본 제공

---

## 📚 추가 학습 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [React 공식 문서](https://react.dev)
- [Kubernetes 공식 문서](https://kubernetes.io/docs)
- [JWT 소개](https://jwt.io/introduction)
- [Docker 공식 문서](https://docs.docker.com)

---

**마지막 업데이트**: 2026년 3월 4일
