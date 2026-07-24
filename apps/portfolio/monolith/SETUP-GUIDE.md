# 풀스택 포트폴리오 개발환경 구축 가이드 (Windows / Mac 공용)

> 한 챕터마다 **Windows용 / Mac용을 나눠서** 정리합니다.
> 어느 OS에서 작업하든 이 문서 하나로 따라 할 수 있습니다.
> 작업을 진행하면서 계속 업데이트됩니다.

**프로젝트 위치**: `C:\dev\portfolio` (Windows) / `~/dev/portfolio` (Mac 권장)
> ⚠️ 한글·공백 경로(예: 바탕화면\한글폴더)는 Docker·Maven·git에서 자주 깨지므로,
> 반드시 영문·공백 없는 경로에서 작업합니다.

---

## 📁 프로젝트 구조

```
portfolio/
├── dev-portfolio/            # React 프론트엔드 (Vite + TS, pnpm)
│   ├── client/               #   실제 화면 코드 (src, public, index.html)
│   ├── package.json
│   └── vite.config.ts
├── dev-portfolio-backend/    # Spring Boot 백엔드 (Java 17)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── docker-compose.yml
├── .gitattributes            # 줄바꿈 정규화 (크로스 플랫폼)
├── .gitignore
└── SETUP-GUIDE.md            # 이 문서
```

## 📌 전체 로드맵

```
[1] Docker Desktop 설치          (실행환경 - 맨 나중에 해도 OK)
[2] 크로스 플랫폼 기본 파일       ← ✅ 완료
[3] 프론트엔드 실행 (Node)        ← ✅ 완료 (localhost:3000)
[4] 백엔드 실행                   ← 다음! (IntelliJ + JDK17, 또는 Docker)
[5] docker-compose 통합
[6] 로컬 완성/테스트
──────────── 배포 단계 ────────────
[7] 배포 대상: AWS / 쿠버네티스
[8] Jenkins (또는 GitHub Actions) CI/CD 자동화
```

### 핵심 개념
| 도구 | 역할 | 비유 |
|------|------|------|
| **Docker** | 앱 패키징 + 실행 환경 (컨테이너 안은 리눅스) | 📦 택배 상자 |
| **Jenkins** | 빌드→테스트→배포 자동화 (CI/CD) | 🤖 자동 배송 로봇 |
| **AWS / K8s** | 앱이 올라가는 배포 대상 서버 | 🏠 배송 도착지 |

- **개발/실행 환경** = 내 PC에서 실행 (`localhost`)
- **배포 환경** = 인터넷 서버 공개 (`https://...`)
- Docker는 둘 다에서 쓰이므로 로컬을 Docker로 만들면 배포도 수월

---

## 챕터 1. Docker Desktop 설치  (실행환경 — 나중에 해도 됨)

컨테이너 안은 **리눅스**. Windows/Mac은 Docker Desktop이 내부에 리눅스를
띄워 실행하므로 **어느 OS든 동일 동작** → Mac↔Windows 작업 가능한 이유.

### 🪟 Windows용
```powershell
# 1. 관리자 터미널에서 WSL2 설치 → 재부팅
wsl --install
# 2. 재부팅 후 Docker Desktop 설치
winget install -e --id Docker.DockerDesktop
# 3. 시작 메뉴에서 Docker Desktop 실행 (🐳)
```

### 🍎 Mac용
```bash
brew install --cask docker   # 설치 (WSL 불필요)
open -a Docker               # 실행
```

### ✅ 설치 확인 (공통)
```bash
docker --version
docker compose version
docker run hello-world
```

### 🔧 트러블슈팅: "Virtualization support not detected" (Windows)
BIOS에서 가상화가 꺼져 있으면 발생. Docker는 가상화 필수.
```powershell
# 진단: False면 BIOS에서 꺼진 상태
powershell -Command "(Get-CimInstance Win32_Processor).VirtualizationFirmwareEnabled"
```
**해결 (BIOS 진입)**:
- 방법 A: 설정 → 시스템 → 복구 → "지금 다시 시작" → 문제 해결 → 고급 옵션 → **UEFI 펌웨어 설정**
- 방법 B: 부팅 시 키 연타 (`Del`/`F2`, HP `F10`, Dell `F2`, Lenovo `F1`)
- BIOS에서 **Advanced/CPU Configuration** → 아래 항목 **Enabled**:
  - Intel: **Intel VT-x / Virtualization Technology**
  - AMD: **SVM Mode / AMD-V**
- **F10** 저장 후 재부팅

> 🍎 Mac은 가상화 기본 활성화라 이 문제 없음.

---

## 챕터 2. 크로스 플랫폼 기본 파일  ✅ 완료

Mac↔Windows 오갈 때 파일 깨짐·불필요한 변경 표시를 막는 파일들.
프로젝트 루트(`C:\dev\portfolio`)에 생성됨. OS 구분 없이 양쪽 공통 적용.

| 파일 | 역할 |
|------|------|
| `.gitattributes` | 줄바꿈(CRLF/LF) 자동 정규화 |
| `.gitignore` | 빌드 결과물·IDE 설정·비밀정보 추적 제외 |

### git 줄바꿈 전역 설정 (선택)
```bash
git config --global core.autocrlf true    # Windows
git config --global core.autocrlf input   # Mac / Linux
```
> `.gitattributes`가 우선하므로 사실상 이 파일 하나로 해결됨.

---

## 챕터 3. 프론트엔드 실행 (Node, Docker 불필요)  ← 다음 단계

프론트엔드는 **pnpm** 패키지 매니저를 사용. Node만 있으면 실행 가능.

> ⚠️ 아래 명령을 복사할 때 **`#` 뒤 설명(주석)은 빼고** 명령만 복사하세요.
> Windows cmd는 `#`를 명령으로 오인해 에러가 납니다.

**🪟 Windows용 (cmd — 한 줄씩 실행)**
```
corepack enable pnpm
cd /d C:\dev\portfolio\dev-portfolio
pnpm install
pnpm dev
```
> `corepack` 권한 에러 시 대체: `npm install -g pnpm`

**🍎 Mac용 (터미널 — 한 줄씩 실행)**
```
corepack enable pnpm
cd ~/dev/portfolio/dev-portfolio
pnpm install
pnpm dev
```

→ 실행 후 브라우저에서 **`http://localhost:3000`** 접속하면 화면 확인 가능.
  (이 프로젝트는 vite.config.ts에서 포트가 **3000**으로 설정되어 있음. 5173 아님)
→ 종료는 터미널에서 `Ctrl + C`.

### 🔧 프론트엔드 설치/실행 트러블슈팅 (실제 겪은 문제)

**① `UNABLE_TO_VERIFY_LEAF_SIGNATURE` (인증서 검증 실패)**
- 원인: 회사 네트워크의 SSL 검사(프록시)가 자체 인증서를 사용 → Node가 검증 실패
- 해결: Node가 OS(Windows) 인증서 저장소를 쓰도록 설정
  ```
  # Windows (cmd) - 영구 설정 후 터미널 재시작
  setx NODE_OPTIONS "--use-system-ca"
  ```
  ```
  # Mac (터미널) - ~/.zshrc 에 추가
  export NODE_OPTIONS=--use-system-ca
  ```
- ⚠️ 이 인증서 문제는 나중에 **Docker 이미지 pull, Maven 다운로드, git**에서도
  나타날 수 있음. 사내망에서 작업 시 공통 원인.

**② `ENOENT: patches/wouter@3.7.1.patch` (없는 패치 참조)**
- 원인: package.json이 존재하지 않는 패치 파일을 참조
- 해결: package.json의 `pnpm.patchedDependencies` 블록 삭제 (완료됨)

**③ 빌드 스크립트 경고 (`@tailwindcss/oxide`, `esbuild`)**
- pnpm이 보안상 빌드 스크립트를 차단하는 경고 → 이 프로젝트는 실행에 지장 없음
- 만약 Tailwind가 깨지면: `pnpm approve-builds` 실행 후 대상 선택

### 📌 실행 방식 팁
- 개발 서버(`pnpm dev`)는 **계속 켜져 있어야** 하는 프로세스
- IntelliJ에서 작업 시: 하단 **Terminal**(`Alt+F12`)에서 `pnpm dev` 실행해두고,
  코드 수정하면 브라우저가 자동 새로고침(HMR)됨

> 이 단계는 Docker/BIOS와 무관하게 지금 바로 가능.
> (백엔드가 아직 안 떠서 로그인/데이터 기능은 나중에 연결)

## 챕터 4. 백엔드 실행 (IntelliJ + JDK 17, Docker 불필요)

백엔드는 **H2 인메모리 DB 내장** → PostgreSQL·Docker 없이 Java만으로 실행됨.
실행 시 자동으로 테이블 생성 + admin 계정(admin/admin) + 샘플 데이터 seed.
- 포트: **8080**, 컨텍스트 경로: **/api** → 프론트의 `localhost:8080/api`와 일치
- ⚠️ H2는 메모리 DB라 **백엔드를 끄면 데이터가 초기화**됨 (개발용으로 정상)

### 🪟 Windows용 / 🍎 Mac용 (IntelliJ 조작은 동일)

**1. 프로젝트 열기**
- IntelliJ 실행 → **File → Open**
- `C:\dev\portfolio\dev-portfolio-backend` 선택 (Mac: `~/dev/portfolio/dev-portfolio-backend`)
- "Trust Project?" → **Trust**

**2. JDK 17 설정 (Java 미설치 시 IntelliJ가 다운로드)**
- **File → Project Structure** (`Ctrl+Alt+Shift+S`, Mac `Cmd+;`)
- **Project → SDK** 드롭다운 → **Add SDK → Download JDK**
- Version **17**, Vendor **Eclipse Temurin** 또는 **Amazon Corretto** → **Download**
- 완료 후 **OK**

**3. Maven 의존성 자동 다운로드 대기**
- 하단 상태바에 다운로드 진행 표시 → 끝날 때까지 대기
- (사내망 인증서 문제로 실패 시 아래 트러블슈팅 참고)

**4. 실행**
- 경로: `src/main/java/com/kimdevops/portfolio/PortfolioBackendApplication.java` 열기
- 클래스 왼쪽/`main` 메서드 옆 **녹색 ▶** 클릭 → **Run 'PortfolioBackendApplication'**
- 콘솔에 아래가 뜨면 성공:
  ```
  Tomcat started on port 8080 (http) with context path '/api'
  Started PortfolioBackendApplication in X.X seconds
  ```

**5. 연결 확인**
- 브라우저에서 프론트(`localhost:3000`) 새로고침 → 프로젝트 목록·데이터가 채워짐
- 로그인 테스트: **admin / admin**
- 백엔드 직접 확인: `http://localhost:8080/api/projects` (JSON 응답)
- API 문서(Swagger): `http://localhost:8080/api/swagger-ui.html`

### 🔧 백엔드 트러블슈팅
| 증상 | 해결 |
|------|------|
| `class X is public, should be declared in a file named X.java` | 중복 "묶음 파일" 문제. 아래 참고 |
| **Maven 도구창이 안 뜸 / 인식 안 됨** | 좌측 트리에서 `pom.xml` **우클릭 → Add as Maven Project**. 또는 View → Tool Windows → Maven 열고 🔄 새로고침 |
| Maven 다운로드 `PKIX path building failed` / `certificate` 에러 | 사내망 인증서 문제. JDK cacerts에 회사 CA 추가 필요 (요청 시 안내) |
| 포트 8080 사용 중 (`Port 8080 was already in use`) | 8080을 쓰는 다른 프로세스(예: 외부 Tomcat)가 있음. 아래 "포트 변경" 참고 |
| 프론트에 데이터 여전히 안 뜸 | 백엔드 콘솔에 "Started" 떴는지 확인, 브라우저 새로고침 |
| `When allowCredentials is true, allowedOrigins cannot contain "*"` (요청마다 500) | CORS 설정 오류. `SecurityConfig`에서 `setAllowedOrigins(... "*")` → `setAllowedOriginPatterns(List.of("*"))` 로 변경 (allowCredentials=true와 와일드카드 병행 가능). 수정 후 백엔드 재시작 |
| 프론트 콘솔 `Unexpected end of JSON input`, 로그인/조회 안 됨, 로그에 `Using generated security password` | `SecurityConfig`에 `SecurityFilterChain`이 없어 Spring Security 기본 보안이 모든 요청을 401(빈 본문)로 차단. → `SecurityFilterChain` 빈 추가(`csrf.disable()` + `anyRequest().permitAll()` + CORS). 수정 후 백엔드 재시작 |
| `xxx.map is not a function` (프론트 크래시) | 목록 API가 배열이 아닌 **페이지 객체**(`{content:[...], totalElements...}`)를 반환하는데 프론트가 배열로 처리. → `Array.isArray(d) ? d : (d?.content ?? [])` 로 방어적 처리 (Spring `Page<T>` 응답 특징) |

**중복 "묶음 파일" 문제 (이 프로젝트에서 실제 발생, 해결 완료)**
- 원인: `Controllers.java`, `Services.java`, `Repositories.java`, `DTOs.java`가
  개별 파일(`AuthController.java` 등)과 **같은 클래스를 중복 정의**.
  게다가 이 묶음 파일들은 pom.xml에 없는 **Lombok**을 사용 → 컴파일 불가.
- 해결: 묶음 파일 4개를 `_backup_duplicate_files/`로 이동(삭제). 개별 파일만 사용.
- (BlogPost 컨트롤러/서비스는 묶음 파일에만 있었고 아무데서도 안 쓰여서 함께 제거됨)

**추가 제거: `ProjectServiceOptimized.java`**
- 원인: 아무데서도 참조 안 되는 "최적화 예시" 잉여 파일인데, 현재
  DTO/엔티티/리포지토리와 안 맞는 코드가 8군데 (없는 메서드 `findByFeaturedTrue()`,
  `countByStatus(String)`, `isFeatured()`, status 타입 불일치 등) → 컴파일 불가.
- 해결: `_backup_duplicate_files/`로 이동. 실제 사용되는 `ProjectService.java`만 유지.
- 교훈: 이 프로젝트는 "개별 파일(정상)" + "묶음/예시 파일(깨짐)"이 섞여 있었음.
  실제 wiring된 개별 파일들만 남기니 정상 컴파일됨.

### 백엔드 포트 변경 (8080 → 8081)
8080을 외부 Tomcat이 점유하고 있어서 백엔드 포트를 8081로 변경함.
**백엔드와 프론트 양쪽을 맞춰야** 연결됨:
1. 백엔드: `application.yml`의 `server.port: ${SERVER_PORT:8081}`
2. 프론트: `dev-portfolio/.env` 파일에 `VITE_API_URL=http://localhost:8081/api`
3. **양쪽 다 재시작** (프론트는 .env를 시작 시 읽으므로 반드시 재시작)
> ⚠️ 나중에 docker-compose(`SERVER_PORT: 8080`)와 k8s 설정도 8080 기준이므로,
> 배포 단계에서는 포트 정책을 다시 통일할 것.

**추가 제거: 테스트 파일 3개** (`ProjectControllerTest`, `ProjectServiceTest`, `ContactServiceTest`)
- 원인: 예전 API 기준으로 작성돼 실제 서비스에 없는 메서드 호출
  (`getAllProjects`, `getProjectById`, `saveContact`, `markAsRead` 등) → 컴파일 불가.
- 해결: `_backup_duplicate_files/`로 이동. (테스트는 앱 실행에 불필요)
- TODO(나중): 실제 API에 맞춰 테스트 재작성 → 포트폴리오 품질 ↑

### 정리된 파일 요약 (`_backup_duplicate_files/`로 이동)
| 파일 | 이유 |
|------|------|
| Controllers/Services/Repositories/DTOs.java | 개별 파일과 중복 + Lombok 미지원 |
| ProjectServiceOptimized.java | 미사용 + DTO/엔티티 불일치 |
| ProjectControllerTest/ProjectServiceTest/ContactServiceTest.java | 예전 API 기준, 컴파일 불가 |

## 챕터 5. 기능 추가하는 법 (예제: 기본정보 Profile)

새 기능 = **백엔드 4~5개 파일 + 프론트 2~3곳**. 앞으로 자격증/게시판도 동일 패턴.

### 백엔드 (Spring Boot) — 도메인 하나당 세트
| 파일 | 역할 |
|------|------|
| `entity/Profile.java` | DB 테이블 설계 (필드 = 컬럼). JPA가 테이블 자동 생성 |
| `repository/ProfileRepository.java` | DB 접근 (`extends JpaRepository`) |
| `service/ProfileService.java` | 비즈니스 로직 (조회/수정) |
| `controller/ProfileController.java` | REST API (`GET/PUT /api/profile`) |
| `config/DataInitializer.java` | 샘플 데이터 seed (선택) |

### 프론트 (React)
| 위치 | 역할 |
|------|------|
| `lib/api.ts` | `profileApi.get()` / `update()` + `ProfileData` 타입 |
| `components/sections/BasicInfoSection.tsx` | 화면 (마운트 시 API 호출 → 표시) |
| `pages/Home.tsx` | 섹션을 페이지에 추가 (`<BasicInfoSection />`) |

### 데이터 흐름
```
BasicInfoSection (마운트)
   → profileApi.get()  →  GET http://localhost:8081/api/profile
   → ProfileController → ProfileService → ProfileRepository → DB(profile 테이블)
   ← JSON { success, data:{name, university, militaryStatus...} }
   → 화면 카드에 렌더링
```

### ⚠️ 새 엔티티/컨트롤러 추가 후에는 백엔드 재시작 필수
- 백엔드는 컴파일 언어(Java)라 코드 바꾸면 **IntelliJ에서 다시 ▶ 실행**해야 반영됨
- 재시작하면 `profile` 테이블이 새로 생성되고 `DataInitializer`가 샘플 데이터 seed
- 프론트(React)는 저장만 하면 **자동 새로고침(HMR)** — 재시작 불필요

## 챕터 6. 관리자 수정 기능 (Admin CRUD UI)

로그인 후 콘텐츠를 수정하려면 **관리 UI(폼)** 가 필요하다. 백엔드 API는 이미 있으므로
프론트에 폼만 붙이면 된다. (예: 기본정보 수정)

### 패턴 (AdminDashboard.tsx)
1. `loadData()` 에서 `profileApi.get()` 으로 현재 값 로드 → `profile` state에 저장
2. 탭 추가(`TabsTrigger` + `TabsContent`) → 입력 폼 렌더 (state와 바인딩)
3. 입력 변경 → `updateProfileField(key, value)` 로 state 갱신
4. 저장 버튼 → `profileApi.update(profile)` (`PUT /api/profile`) → 성공 시 toast
5. 홈의 `BasicInfoSection` 은 다음 로드 때 새 값 표시

### ⚠️ H2 인메모리 주의 — 수정해도 재시작하면 초기화됨
- 지금 DB는 H2 인메모리 → 백엔드를 껐다 켜면 **수정한 내용이 사라지고** seed 값(김개발)으로 돌아감
- 실제로 내 정보를 **영구 저장**하려면:
  - (간단) 파일 기반 H2: `jdbc:h2:file:./data/portfolio` 로 변경
  - (권장) PostgreSQL(Docker) 로 전환 — 배포 단계에서 진행
- 또는 seed 값(`DataInitializer.createSampleProfile`)을 본인 정보로 바꿔두면 재시작해도 그 값으로 시작

## 챕터 7. 파일 업로드 + 경력/사진/현재준비중

### 파일 업로드 (이미지)
- 백엔드: `FileController`(POST `/api/files/upload`, MultipartFile) → `./uploads/`에 저장 →
  접근 URL 반환. `WebConfig`가 `/api/uploads/**`로 정적 서빙.
- 설정: `application.yml`에 `spring.servlet.multipart.max-file-size: 10MB`
- 프론트: `fileApi.upload(file)` (FormData, Content-Type 지정 안 함 → 브라우저가 boundary 자동 설정)
- `uploads/`, `data/`는 `.gitignore`에 추가 (로컬 파일이라 커밋 안 함)

### 새 기능 (모두 기존 CRUD 패턴)
| 기능 | 엔티티 | 화면 |
|------|--------|------|
| 경력(Career) | `Career`(회사/직무/기간/업무) | `CareerSection`(타임라인) + 관리자 경력탭 |
| 프로필 사진 | `Profile.imageUrl` | 업로드 후 BasicInfoSection에 원형 표시 |
| 현재 준비중 | `Profile.currentStatus` | BasicInfoSection에 배지로 표시 |

### 이미지 표시 관련 팁
- 업로드 응답은 전체 URL(`http://localhost:8081/api/uploads/xxx.png`) → `profile.imageUrl`에 저장
- `<img>` 태그는 CORS 제약 없이 다른 포트 이미지도 로드 가능
- 사진 선택 → 업로드 → **저장 버튼**을 눌러야 프로필에 최종 반영됨

## 챕터 8. 개발 노트(DevNote) — 트러블슈팅 케이스 증빙

포트폴리오에서 "어떻게 설계·구현하고, 어떤 문제를 어떻게 해결했는지"를
코드와 함께 보여주는 기능. (채용 관점에서 가장 설득력 있는 콘텐츠)

- 엔티티 `DevNote`: title, category, situation(상황), codeBefore, codeAfter, solution
- API: GET/POST/**PUT**/DELETE (긴 글이라 수정 기능 포함)
- 홈: `DevNotesSection` — 상황 → Before/After 코드 → 원인&해결 카드
- 관리자: 개발노트 탭 — 작성/수정(폼에 로드)/삭제
- 시드: 오늘 실제로 해결한 4건 (CORS / SecurityFilterChain / H2 영구저장 / 모놀리식 설계)

> 이 시드 내용은 **실제로 발생하고 해결한 것**이라 그대로 "증빙"이 됨.
> Java 17 텍스트 블록(`""" ... """`)으로 코드 스니펫을 깔끔하게 시드.

## 챕터 9. 로그인 보안(JWT) + 기술스택 DB화

### JWT 인증 (조회는 공개, 쓰기/관리는 로그인 필요)
- `JwtAuthenticationFilter`: `Authorization: Bearer <token>` 검증 → SecurityContext에 인증 세팅
- `SecurityConfig`: 규칙 적용
  - 공개: `POST /auth/login`·`/contacts`·`/visitors`, `GET` 포트폴리오 조회, `/uploads/**`, 문서
  - 인증 필요: 그 외 모든 쓰기(POST/PUT/DELETE) + 관리자 조회(GET /contacts, /visitors/stats)
- 프론트는 이미 쓰기/관리 호출에 토큰을 보냄 → 로그인 후 정상 동작
- ⚠️ 재시작 후 **관리자 기능은 로그인(admin/admin) 필요**. 홈 조회는 로그인 없이 됨.

### 기술스택 DB화 (하드코딩 → 관리)
- `Skill` 엔티티(category/name/level/color) + API + 시드
- `SkillsSection`을 `/api/skills`에서 받아 카테고리별 렌더로 교체 (하드코딩·가짜 자격증 블록 제거)
- 관리자 "스킬" 탭에서 추가/삭제 (색상·숙련도 포함)

### 남은 가짜 데이터 (다음 작업)
- `HeroSection`(첫 화면 문구), `AboutSection`(가짜 통계 5년/30+/50+) → Profile 데이터로 연결 예정

<!-- 다음 챕터부터 여기에 계속 추가됩니다 -->
