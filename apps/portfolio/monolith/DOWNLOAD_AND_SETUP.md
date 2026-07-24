# 프로젝트 다운로드 및 로컬 설정 가이드

## 📥 파일 다운로드

### 방법 1: Management UI에서 다운로드 (추천)

1. **프론트엔드 다운로드**
   - Manus Management UI 열기
   - "Code" 패널 클릭
   - "Download all files" 클릭
   - `dev-portfolio-client.zip` 다운로드

2. **백엔드 다운로드**
   - 다음 경로의 파일들을 수동으로 다운로드하거나
   - GitHub로 내보내기 (아래 참고)

### 방법 2: GitHub로 내보내기

1. **Management UI에서 GitHub 연동**
   ```
   Settings → GitHub → Export to GitHub
   ```

2. **저장소 클론**
   ```bash
   git clone https://github.com/your-username/dev-portfolio.git
   cd dev-portfolio
   ```

### 방법 3: 직접 파일 복사

**프론트엔드:**
```bash
# 프론트엔드 디렉토리 구조
dev-portfolio/
├── client/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── tailwind.config.ts
├── server/
├── shared/
└── README.md
```

**백엔드:**
```bash
# 백엔드 디렉토리 구조
dev-portfolio-backend/
├── src/
│   ├── main/
│   │   ├── java/com/kimdevops/portfolio/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── controller/
│   │   │   ├── config/
│   │   │   ├── security/
│   │   │   └── PortfolioBackendApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── k8s/
└── README.md
```

---

## 🚀 로컬 환경 설정

### 사전 요구사항

```bash
# 확인 명령어
node --version      # v18.0.0 이상
npm --version       # 9.0.0 이상
java -version       # JDK 17 이상
mvn --version       # 3.8.0 이상
```

### 1. 프론트엔드 설정

**Step 1: 의존성 설치**
```bash
cd dev-portfolio/client

# pnpm 사용 (추천)
pnpm install

# 또는 npm 사용
npm install

# 또는 yarn 사용
yarn install
```

**Step 2: 환경 변수 설정**
```bash
# .env.local 파일 생성
cat > .env.local << EOF
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=Dev Portfolio
VITE_APP_LOGO=/logo.svg
EOF
```

**Step 3: 개발 서버 실행**
```bash
# 개발 서버 시작
pnpm dev

# 또는
npm run dev
```

**접속 주소**: `http://localhost:5173`

**주요 npm 스크립트:**
```bash
pnpm dev       # 개발 서버 시작
pnpm build     # 프로덕션 빌드
pnpm preview   # 빌드 결과 미리보기
pnpm check     # TypeScript 타입 체크
pnpm format    # 코드 포매팅
```

### 2. 백엔드 설정

**Step 1: 의존성 다운로드**
```bash
cd dev-portfolio-backend

# Maven 의존성 다운로드
mvn clean install -DskipTests
```

**Step 2: 환경 변수 설정**
```bash
# application.yml 확인 (기본값 사용 가능)
cat src/main/resources/application.yml
```

**H2 데이터베이스 사용 (기본)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**PostgreSQL 사용 (프로덕션)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/portfolio_db
    username: portfolio_user
    password: portfolio_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
```

**Step 3: 애플리케이션 빌드**
```bash
# JAR 파일 생성
mvn clean package -DskipTests

# 빌드 결과 확인
ls -lh target/portfolio-backend-1.0.0.jar
```

**Step 4: 애플리케이션 실행**
```bash
# JAR 실행
java -jar target/portfolio-backend-1.0.0.jar

# 또는 IDE에서 실행
# PortfolioBackendApplication.java 우클릭 → Run
```

**접속 주소**: `http://localhost:8080/api`

**주요 Maven 명령어:**
```bash
mvn clean package -DskipTests    # 빌드
mvn clean install                # 빌드 및 설치
mvn test                         # 테스트 실행
mvn spring-boot:run              # 직접 실행
mvn dependency:tree              # 의존성 트리 확인
```

---

## 🔐 기본 인증 정보

**로그인 페이지**: `http://localhost:5173/login`

```
Username: admin
Password: admin
```

**관리자 대시보드**: `http://localhost:5173/admin`

---

## 🗄️ 데이터베이스 설정

### H2 콘솔 접속 (개발 중)

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (비워두기)
```

### PostgreSQL 설정 (프로덕션)

**1. PostgreSQL 설치**
```bash
# macOS
brew install postgresql

# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib

# Windows
# https://www.postgresql.org/download/windows/
```

**2. 데이터베이스 생성**
```bash
# PostgreSQL 접속
psql -U postgres

# 데이터베이스 생성
CREATE DATABASE portfolio_db;

# 사용자 생성
CREATE USER portfolio_user WITH PASSWORD 'portfolio_password';

# 권한 부여
ALTER ROLE portfolio_user SET client_encoding TO 'utf8';
ALTER ROLE portfolio_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE portfolio_user SET default_transaction_deferrable TO on;
ALTER ROLE portfolio_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE portfolio_db TO portfolio_user;

# 종료
\q
```

**3. application.yml 수정**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/portfolio_db
    username: portfolio_user
    password: portfolio_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 🐳 Docker로 실행

### Docker Compose 사용

**1. 파일 준비**
```bash
cd dev-portfolio-backend

# docker-compose.yml 확인
cat docker-compose.yml
```

**2. 서비스 시작**
```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 서비스 상태 확인
docker-compose ps
```

**3. 서비스 접속**
```
Frontend: http://localhost:5173
Backend: http://localhost:8080/api
PostgreSQL: localhost:5432
```

**4. 서비스 중지**
```bash
# 모든 서비스 중지
docker-compose down

# 볼륨 포함 삭제
docker-compose down -v
```

---

## 🧪 API 테스트

### cURL 사용

**1. 로그인**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**응답:**
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

**2. 프로젝트 조회**
```bash
curl http://localhost:8080/api/projects
```

**3. 연락처 제출**
```bash
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "subject": "Project Inquiry",
    "message": "I am interested in your services"
  }'
```

**4. 방문자 기록**
```bash
curl -X POST http://localhost:8080/api/visitors \
  -H "Content-Type: application/json" \
  -d '{
    "page": "/",
    "sessionId": "session-123"
  }'
```

**5. 통계 조회 (관리자)**
```bash
curl http://localhost:8080/api/visitors/stats \
  -H "Authorization: Bearer <token>"
```

### Postman 사용

1. **Postman 설치**: https://www.postman.com/downloads/
2. **컬렉션 import**:
   - File → Import
   - 아래 JSON 복사 → Paste raw text

**Postman 컬렉션:**
```json
{
  "info": {
    "name": "Dev Portfolio API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "url": "http://localhost:8080/api/auth/login",
            "body": {
              "mode": "raw",
              "raw": "{\"username\":\"admin\",\"password\":\"admin\"}"
            }
          }
        }
      ]
    },
    {
      "name": "Projects",
      "item": [
        {
          "name": "Get All",
          "request": {
            "method": "GET",
            "url": "http://localhost:8080/api/projects"
          }
        }
      ]
    }
  ]
}
```

---

## 🔧 IDE 설정

### VS Code (프론트엔드)

**추천 확장프로그램:**
```
- ES7+ React/Redux/React-Native snippets
- TypeScript Vue Plugin
- Tailwind CSS IntelliSense
- Prettier - Code formatter
- ESLint
```

**settings.json:**
```json
{
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.formatOnSave": true,
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "tailwindCSS.experimental.classRegex": [
    ["cva\\(([^)]*)\\)", "(?:'|\"|`)([^']*)(?:'|\"|`)"]
  ]
}
```

### IntelliJ IDEA (백엔드)

**추천 플러그인:**
```
- Spring Boot
- Spring Data JPA
- Lombok
- CheckStyle-IDEA
- SonarLint
```

**실행 구성:**
```
Run → Edit Configurations
- Main class: com.kimdevops.portfolio.PortfolioBackendApplication
- VM options: -Dspring.profiles.active=dev
- Environment variables: JWT_SECRET=dev-secret
```

---

## 🚨 트러블슈팅

### 포트 충돌

**문제**: Port 8080 already in use

**해결책:**
```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>

# 또는 다른 포트 사용
java -jar target/portfolio-backend-1.0.0.jar --server.port=8081
```

### 의존성 오류

**문제**: Maven 의존성 다운로드 실패

**해결책:**
```bash
# Maven 캐시 삭제
rm -rf ~/.m2/repository

# 다시 다운로드
mvn clean install -DskipTests
```

### 데이터베이스 연결 오류

**문제**: Cannot connect to database

**해결책:**
```bash
# PostgreSQL 상태 확인
sudo systemctl status postgresql

# PostgreSQL 시작
sudo systemctl start postgresql

# 또는 H2 사용 (기본값)
# application.yml에서 H2 설정 확인
```

### CORS 오류

**문제**: Access to XMLHttpRequest blocked by CORS policy

**해결책:**
```typescript
// SecurityConfig.java에서 CORS 설정 확인
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173"
));
```

### 토큰 만료

**문제**: 401 Unauthorized

**해결책:**
```bash
# 다시 로그인하여 새 토큰 발급
# localStorage에서 이전 토큰 삭제
localStorage.removeItem('token');
```

---

## 📚 추가 리소스

- [프로젝트 README](./README.md)
- [기술 스택 가이드](./TECH_STACK_GUIDE.md)
- [배포 가이드](./DEPLOYMENT_GUIDE.md)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [React 공식 문서](https://react.dev)

---

**마지막 업데이트**: 2026년 3월 4일
