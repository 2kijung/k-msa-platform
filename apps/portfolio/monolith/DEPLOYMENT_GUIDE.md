# 풀스택 포트폴리오 배포 가이드

## 📋 프로젝트 구조

```
dev-portfolio/
├── client/                    # React + TypeScript 프론트엔드
│   ├── src/
│   │   ├── pages/            # 페이지 (Home, AdminDashboard, LoginPage)
│   │   ├── components/       # UI 컴포넌트
│   │   ├── lib/api.ts        # 백엔드 API 통신
│   │   └── App.tsx           # 라우팅 설정
│   └── package.json
│
└── dev-portfolio-backend/    # Spring Boot 백엔드
    ├── src/main/java/
    │   └── com/kimdevops/portfolio/
    │       ├── entity/       # JPA 엔티티 (User, Project, Contact, etc.)
    │       ├── repository/   # Spring Data JPA Repository
    │       ├── service/      # 비즈니스 로직
    │       ├── controller/   # REST API 엔드포인트
    │       ├── config/       # 설정 (Security, DataInitializer)
    │       └── security/     # JWT 유틸리티
    ├── Dockerfile
    ├── docker-compose.yml
    ├── pom.xml
    └── k8s/                  # Kubernetes 배포 설정
```

---

## 🚀 로컬 개발 환경 실행

### 1. 백엔드 실행 (Spring Boot)

```bash
cd dev-portfolio-backend

# Maven 빌드
mvn clean package -DskipTests

# JAR 실행
java -jar target/portfolio-backend-1.0.0.jar
```

**백엔드 주소**: `http://localhost:8080/api`

**기본 인증 정보**:
- Username: `admin`
- Password: `admin`

### 2. 프론트엔드 실행 (React)

```bash
cd dev-portfolio/client

# 의존성 설치
pnpm install

# 개발 서버 시작
pnpm dev
```

**프론트엔드 주소**: `http://localhost:5173`

---

## 🐳 Docker Compose로 실행

### 1. Docker Compose 파일 준비

```bash
cd dev-portfolio-backend
```

### 2. 서비스 시작

```bash
docker-compose up -d
```

**서비스 주소**:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

### 3. 서비스 중지

```bash
docker-compose down
```

---

## ☸️ Kubernetes 배포

### 1. 사전 요구사항

- Kubernetes 클러스터 (1.20+)
- `kubectl` CLI
- Docker Registry (DockerHub, ECR, GCR 등)

### 2. Docker 이미지 빌드 및 푸시

```bash
cd dev-portfolio-backend

# 이미지 빌드
docker build -t your-registry/dev-portfolio-backend:latest .

# 이미지 푸시
docker push your-registry/dev-portfolio-backend:latest
```

### 3. Kubernetes 배포

```bash
# Namespace 생성
kubectl create namespace portfolio

# ConfigMap 생성
kubectl create configmap portfolio-config \
  --from-literal=DB_HOST=postgres \
  --from-literal=DB_PORT=5432 \
  --from-literal=DB_NAME=portfolio_db \
  -n portfolio

# Secret 생성
kubectl create secret generic portfolio-secrets \
  --from-literal=DB_USER=portfolio_user \
  --from-literal=DB_PASSWORD=your-secure-password \
  --from-literal=JWT_SECRET=your-jwt-secret \
  -n portfolio

# 배포
kubectl apply -f k8s/deployment.yaml -n portfolio
kubectl apply -f k8s/service.yaml -n portfolio
```

### 4. 배포 상태 확인

```bash
# Pod 상태 확인
kubectl get pods -n portfolio

# 서비스 확인
kubectl get svc -n portfolio

# 로그 확인
kubectl logs -f deployment/portfolio-backend -n portfolio
```

---

## 🔧 환경 변수 설정

### 백엔드 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DB_HOST` | 데이터베이스 호스트 | `localhost` |
| `DB_PORT` | 데이터베이스 포트 | `5432` |
| `DB_NAME` | 데이터베이스 이름 | `portfolio_db` |
| `DB_USER` | 데이터베이스 사용자 | `portfolio_user` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `portfolio_password` |
| `SERVER_PORT` | 서버 포트 | `8080` |
| `JWT_SECRET` | JWT 서명 키 | `your-secret-key` |

### 프론트엔드 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `VITE_API_URL` | 백엔드 API URL | `http://localhost:8080/api` |

---

## 📊 API 엔드포인트

### 인증
- `POST /auth/login` - 로그인
- `POST /auth/logout` - 로그아웃

### 연락처
- `GET /contacts` - 모든 연락처 조회 (관리자)
- `POST /contacts` - 새 연락처 생성

### 프로젝트
- `GET /projects` - 모든 프로젝트 조회
- `POST /projects` - 새 프로젝트 생성 (관리자)
- `PUT /projects/{id}` - 프로젝트 수정 (관리자)
- `DELETE /projects/{id}` - 프로젝트 삭제 (관리자)

### 방문자 통계
- `POST /visitors` - 방문 기록
- `GET /visitors/stats` - 통계 조회 (관리자)

---

## 🔐 보안 체크리스트

- [ ] JWT_SECRET을 강력한 값으로 변경
- [ ] 데이터베이스 비밀번호를 강력한 값으로 변경
- [ ] HTTPS 활성화
- [ ] CORS 설정 검토
- [ ] 환경 변수를 환경에 맞게 설정
- [ ] 데이터베이스 백업 설정
- [ ] 로그 모니터링 설정

---

## 📈 성능 최적화

### 프론트엔드
- Vite를 사용한 빠른 빌드
- React 18의 자동 배치 최적화
- 코드 분할 및 레이지 로딩

### 백엔드
- H2 인메모리 DB (개발용) → PostgreSQL (프로덕션)
- JPA 쿼리 최적화
- 캐싱 전략 구현
- 데이터베이스 인덱싱

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
```bash
# PostgreSQL 상태 확인
docker ps | grep postgres

# 데이터베이스 재시작
docker-compose restart postgres
```

### API 호출 실패
- CORS 설정 확인
- API URL 확인
- 토큰 유효성 확인

---

## 📚 추가 리소스

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [React 공식 문서](https://react.dev)
- [Kubernetes 공식 문서](https://kubernetes.io/docs)
- [Docker 공식 문서](https://docs.docker.com)

---

## 📞 지원

문제가 발생하면:
1. 로그 확인
2. 환경 변수 확인
3. 포트 충돌 확인
4. 데이터베이스 연결 확인
