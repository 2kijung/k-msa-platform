# Dev Portfolio Backend

Spring Boot 3.x + JDK 17 기반의 풀스택 포트폴리오 백엔드 서버입니다.

## 기술 스택

- **Runtime**: JDK 17 (LTS)
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 16
- **Security**: Spring Security + JWT
- **Container**: Docker
- **Orchestration**: Kubernetes + Helm
- **Build**: Maven

## 주요 기능

1. **Contact Form Management** - 문의 폼 제출 및 상태 관리
2. **Blog Post CRUD** - 기술 블로그 포스트 작성/수정/삭제
3. **Project Management** - 포트폴리오 프로젝트 동적 관리
4. **Visitor Analytics** - 방문자 통계 및 분석
5. **User Authentication** - JWT 기반 사용자 인증
6. **Admin Dashboard** - 관리자 대시보드 API

## 데이터베이스 스키마

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  role VARCHAR(50) NOT NULL,
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Contacts Table
```sql
CREATE TABLE contacts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(50) DEFAULT 'NEW',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Blog Posts Table
```sql
CREATE TABLE blog_posts (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  content TEXT NOT NULL,
  excerpt TEXT,
  category VARCHAR(100),
  tags VARCHAR(255),
  author_id BIGINT NOT NULL REFERENCES users(id),
  status VARCHAR(50) DEFAULT 'DRAFT',
  view_count BIGINT DEFAULT 0,
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Projects Table
```sql
CREATE TABLE projects (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  long_description TEXT,
  technologies VARCHAR(255),
  status VARCHAR(50) DEFAULT 'DEVELOPMENT',
  image_url VARCHAR(500),
  github_url VARCHAR(500),
  live_url VARCHAR(500),
  metrics TEXT,
  featured BOOLEAN DEFAULT false,
  display_order INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Visitors Table
```sql
CREATE TABLE visitors (
  id BIGSERIAL PRIMARY KEY,
  ip_address VARCHAR(45) NOT NULL,
  user_agent TEXT,
  referer VARCHAR(500),
  page VARCHAR(255),
  country VARCHAR(100),
  city VARCHAR(100),
  session_id VARCHAR(255),
  visited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  duration_seconds BIGINT
);
```

## API 엔드포인트

### Authentication
- `POST /api/auth/login` - 사용자 로그인
- `GET /api/auth/health` - 헬스 체크

### Contacts
- `POST /api/contacts` - 문의 제출
- `GET /api/contacts` - 문의 목록 조회 (페이징)
- `PUT /api/contacts/{id}/status` - 문의 상태 업데이트

### Blog Posts
- `POST /api/blog` - 블로그 포스트 작성
- `GET /api/blog` - 발행된 포스트 조회 (페이징)
- `PUT /api/blog/{id}` - 포스트 수정
- `DELETE /api/blog/{id}` - 포스트 삭제

### Projects
- `POST /api/projects` - 프로젝트 추가
- `GET /api/projects` - 프로젝트 목록 조회 (페이징)
- `GET /api/projects/featured` - 주요 프로젝트 조회
- `PUT /api/projects/{id}` - 프로젝트 수정
- `DELETE /api/projects/{id}` - 프로젝트 삭제

### Visitors
- `POST /api/visitors` - 방문 기록
- `GET /api/visitors/stats` - 방문자 통계

## 로컬 개발 환경 설정

### 필수 요구사항
- JDK 17 이상
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (또는 Docker 사용)

### 실행 방법

#### 1. Docker Compose로 실행 (권장)
```bash
cd /home/ubuntu/dev-portfolio-backend
docker-compose up -d
```

#### 2. 로컬 Maven 빌드
```bash
mvn clean install
mvn spring-boot:run
```

#### 3. Docker 이미지 빌드
```bash
docker build -t portfolio-backend:latest .
docker run -p 8080:8080 \
  -e DB_HOST=localhost \
  -e DB_USER=portfolio_user \
  -e DB_PASSWORD=portfolio_password \
  portfolio-backend:latest
```

## Kubernetes 배포

### 1. 이미지 빌드 및 레지스트리 푸시
```bash
docker build -t your-registry/portfolio-backend:1.0.0 .
docker push your-registry/portfolio-backend:1.0.0
```

### 2. Kubernetes 매니페스트 적용
```bash
kubectl apply -f k8s/deployment.yaml
```

### 3. Helm Chart로 배포
```bash
helm install portfolio-release ./helm \
  --namespace portfolio \
  --values helm/values.yaml
```

### 4. 배포 상태 확인
```bash
kubectl get deployments -n portfolio
kubectl get services -n portfolio
kubectl get pods -n portfolio
```

### 5. 포트 포워딩 (로컬 테스트)
```bash
kubectl port-forward -n portfolio svc/portfolio-backend 8080:8080
```

## 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DB_HOST` | PostgreSQL 호스트 | localhost |
| `DB_PORT` | PostgreSQL 포트 | 5432 |
| `DB_NAME` | 데이터베이스 이름 | portfolio_db |
| `DB_USER` | 데이터베이스 사용자 | portfolio_user |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | portfolio_password |
| `SERVER_PORT` | 서버 포트 | 8080 |
| `JWT_SECRET` | JWT 서명 키 | (필수) |
| `JWT_EXPIRATION` | JWT 만료 시간 (ms) | 86400000 |

## 모니터링 및 로깅

### 로그 레벨 설정
```yaml
logging:
  level:
    root: INFO
    com.kimdevops: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### Kubernetes 로그 조회
```bash
kubectl logs -n portfolio deployment/portfolio-backend
kubectl logs -n portfolio deployment/portfolio-backend --tail=100 -f
```

## 보안 고려사항

1. **JWT Secret 변경**: 프로덕션 환경에서는 반드시 강력한 시크릿 키 설정
2. **CORS 설정**: `application.yml`에서 허용 도메인 제한
3. **데이터베이스 암호화**: PostgreSQL SSL 연결 설정
4. **API 인증**: 민감한 엔드포인트에 JWT 인증 추가
5. **HTTPS**: 프로덕션에서는 반드시 HTTPS 사용

## 성능 최적화

- **데이터베이스 인덱싱**: 주요 쿼리 필드에 인덱스 설정
- **캐싱**: Redis 캐싱 추가 가능
- **Connection Pooling**: HikariCP 자동 설정
- **Kubernetes HPA**: CPU/메모리 기반 자동 스케일링

## 문제 해결

### 데이터베이스 연결 실패
```bash
# PostgreSQL 상태 확인
docker ps | grep postgres

# 데이터베이스 접속 테스트
psql -h localhost -U portfolio_user -d portfolio_db
```

### 포트 충돌
```bash
# 포트 사용 상태 확인
lsof -i :8080
lsof -i :5432

# 포트 변경 (docker-compose.yml 수정)
ports:
  - "8081:8080"  # 8081로 변경
```

### Kubernetes Pod 상태 확인
```bash
kubectl describe pod -n portfolio <pod-name>
kubectl logs -n portfolio <pod-name>
```

## 라이선스

MIT License

## 연락처

kim@devops.kr
