# 웹개발자 포트폴리오 - 풀스택 애플리케이션

> **JDK 17 + Spring Boot + MSA + Docker + Kubernetes 기반 실제 동작하는 포트폴리오 시스템**

## 🎯 프로젝트 개요

이 프로젝트는 웹개발자의 포트폴리오를 관리하고 전시하는 **완전한 풀스택 애플리케이션**입니다. 현대적인 기술 스택을 활용하여 구축되었으며, 실제 프로덕션 환경에 배포 가능합니다.

### ✨ 주요 특징

- **React + TypeScript 프론트엔드**: 모던한 UI/UX와 타입 안정성
- **Spring Boot 3.x 백엔드**: JDK 17 기반의 강력한 API 서버
- **JWT 인증**: 안전한 사용자 인증 및 권한 관리
- **H2 + PostgreSQL**: 개발용 인메모리 DB와 프로덕션 관계형 DB 지원
- **Docker & Kubernetes**: 컨테이너화 및 오케스트레이션 지원
- **관리자 대시보드**: 포트폴리오 콘텐츠 관리 시스템
- **방문자 통계**: 실시간 포트폴리오 방문 분석

---

## 🏗️ 기술 스택

### 프론트엔드
- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빠른 빌드 도구
- **Tailwind CSS 4** - 유틸리티 기반 스타일링
- **shadcn/ui** - 고급 UI 컴포넌트
- **Wouter** - 클라이언트 라우팅

### 백엔드
- **Spring Boot 3.2** - 웹 애플리케이션 프레임워크
- **Spring Data JPA** - ORM 및 데이터 접근
- **Spring Security** - 인증 및 권한 관리
- **JWT (JJWT)** - JSON Web Token 기반 인증
- **H2 Database** - 개발용 인메모리 DB
- **PostgreSQL** - 프로덕션 관계형 DB

### 배포 & 인프라
- **Docker** - 컨테이너화
- **Docker Compose** - 다중 컨테이너 오케스트레이션
- **Kubernetes** - 프로덕션 오케스트레이션
- **Helm** - Kubernetes 패키지 관리

---

## 📁 프로젝트 구조

```
.
├── dev-portfolio/                 # React 프론트엔드
│   ├── client/
│   │   ├── src/
│   │   │   ├── pages/            # 페이지 컴포넌트
│   │   │   ├── components/       # 재사용 가능한 컴포넌트
│   │   │   ├── lib/api.ts        # API 통신 레이어
│   │   │   ├── App.tsx           # 라우팅 설정
│   │   │   └── index.css         # 글로벌 스타일
│   │   └── package.json
│   └── README.md
│
├── dev-portfolio-backend/         # Spring Boot 백엔드
│   ├── src/main/java/
│   │   └── com/kimdevops/portfolio/
│   │       ├── entity/           # JPA 엔티티
│   │       ├── repository/       # Spring Data JPA
│   │       ├── service/          # 비즈니스 로직
│   │       ├── controller/       # REST 컨트롤러
│   │       ├── config/           # 설정 클래스
│   │       └── security/         # JWT 보안
│   ├── src/main/resources/
│   │   └── application.yml       # 애플리케이션 설정
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── k8s/                      # Kubernetes 매니페스트
│   └── README.md
│
├── DEPLOYMENT_GUIDE.md           # 배포 가이드
└── README.md                     # 이 파일
```

---

## 🚀 빠른 시작

### 1. 로컬 개발 환경 설정

#### 백엔드 실행
```bash
cd dev-portfolio-backend

# Maven 빌드
mvn clean package -DskipTests

# 애플리케이션 시작
java -jar target/portfolio-backend-1.0.0.jar
```

**백엔드 주소**: `http://localhost:8080/api`

#### 프론트엔드 실행
```bash
cd dev-portfolio/client

# 의존성 설치
pnpm install

# 개발 서버 시작
pnpm dev
```

**프론트엔드 주소**: `http://localhost:5173`

### 2. 기본 인증 정보

```
Username: admin
Password: admin
```

### 3. 관리자 대시보드 접근

1. 프론트엔드 열기: `http://localhost:5173`
2. 네비게이션 바의 "Admin" 버튼 클릭
3. 로그인 페이지에서 기본 인증 정보 입력
4. 대시보드에서 포트폴리오 콘텐츠 관리

---

## 📚 API 문서

### 인증 엔드포인트

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

**응답**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@portfolio.com",
      "role": "ADMIN"
    }
  }
}
```

### 프로젝트 엔드포인트

#### 모든 프로젝트 조회
```http
GET /api/projects
```

#### 새 프로젝트 생성 (관리자)
```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "프로젝트명",
  "description": "설명",
  "technologies": "Spring Boot, React, Docker",
  "status": "PRODUCTION",
  "featured": true
}
```

### 연락처 엔드포인트

#### 연락처 제출
```http
POST /api/contacts
Content-Type: application/json

{
  "name": "이름",
  "email": "email@example.com",
  "subject": "주제",
  "message": "메시지"
}
```

#### 모든 연락처 조회 (관리자)
```http
GET /api/contacts?page=0&size=10
Authorization: Bearer <token>
```

### 방문자 통계 엔드포인트

#### 방문 기록
```http
POST /api/visitors
Content-Type: application/json

{
  "page": "/",
  "sessionId": "session-id"
}
```

#### 통계 조회 (관리자)
```http
GET /api/visitors/stats
Authorization: Bearer <token>
```

---

## 🐳 Docker 배포

### Docker Compose로 실행

```bash
cd dev-portfolio-backend

# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down
```

**서비스 주소**:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

---

## ☸️ Kubernetes 배포

### 사전 요구사항
- Kubernetes 클러스터 (1.20+)
- kubectl CLI
- Docker Registry

### 배포 단계

```bash
# 1. Namespace 생성
kubectl create namespace portfolio

# 2. ConfigMap 생성
kubectl create configmap portfolio-config \
  --from-literal=DB_HOST=postgres \
  --from-literal=DB_PORT=5432 \
  -n portfolio

# 3. Secret 생성
kubectl create secret generic portfolio-secrets \
  --from-literal=DB_PASSWORD=your-password \
  --from-literal=JWT_SECRET=your-secret \
  -n portfolio

# 4. 배포
kubectl apply -f dev-portfolio-backend/k8s/ -n portfolio

# 5. 상태 확인
kubectl get pods -n portfolio
kubectl get svc -n portfolio
```

---

## 🔐 보안

### JWT 토큰
- 발급: 로그인 시
- 유효 기간: 24시간
- 서명: HS256 알고리즘

### 비밀번호 암호화
- 알고리즘: BCrypt
- 강도: 10 라운드

### CORS 설정
- 허용 출처: `http://localhost:3000`, `http://localhost:5173`
- 허용 메서드: GET, POST, PUT, DELETE, OPTIONS
- 허용 헤더: 모두

---

## 📊 데이터베이스 스키마

### Users 테이블
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL
);
```

### Projects 테이블
```sql
CREATE TABLE projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  technologies TEXT,
  status VARCHAR(50),
  featured BOOLEAN,
  display_order INT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);
```

### Contacts 테이블
```sql
CREATE TABLE contacts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  subject VARCHAR(255),
  message TEXT,
  status VARCHAR(50),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);
```

### Visitors 테이블
```sql
CREATE TABLE visitors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ip_address VARCHAR(45),
  page VARCHAR(255),
  session_id VARCHAR(255),
  visited_at TIMESTAMP NOT NULL
);
```

---

## 🧪 테스트

### 프론트엔드 테스트
```bash
cd dev-portfolio/client
pnpm test
```

### 백엔드 테스트
```bash
cd dev-portfolio-backend
mvn test
```

---

## 📈 성능 최적화

### 프론트엔드
- Vite를 통한 빠른 개발 빌드
- React 18 자동 배치 최적화
- 코드 분할 및 레이지 로딩
- 이미지 최적화

### 백엔드
- JPA 쿼리 최적화
- 데이터베이스 인덱싱
- 캐싱 전략
- 연결 풀 관리

---

## 🐛 트러블슈팅

### 포트 충돌
```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

### 데이터베이스 연결 실패
- PostgreSQL 서비스 상태 확인
- 환경 변수 확인
- 방화벽 설정 확인

### API 호출 실패
- CORS 설정 확인
- 토큰 유효성 확인
- 네트워크 연결 확인

---

## 📚 추가 리소스

- [배포 가이드](./DEPLOYMENT_GUIDE.md)
- [Spring Boot 문서](https://spring.io/projects/spring-boot)
- [React 문서](https://react.dev)
- [Kubernetes 문서](https://kubernetes.io/docs)

---

## 📝 라이센스

MIT License - 자유롭게 사용, 수정, 배포 가능합니다.

---

## 👨‍💻 개발자

**Kim DevOps** - Full Stack Developer
- 기술 스택: JDK 17, Spring Boot, React, Docker, Kubernetes
- 이메일: admin@portfolio.com

---

## 🙏 감사의 말

- Spring Boot 팀
- React 팀
- Kubernetes 커뮤니티
- 모든 오픈소스 기여자들

---

**마지막 업데이트**: 2026년 3월 4일
