# 통합 MSA 마이그레이션 설계서

> **대상**: `C:\dev\portfolio` (Spring Boot 3.2 모놀리식 + React) → `C:\openclaw-msa` (Spring Framework 5.3 MSA 인프라)
> **목표**: portfolio의 실질 기능을 openclaw-msa의 MSA 인프라 위로 이관하되, 무리한 전면 재작성 없이 점진 전환한다.
> **프로젝트 성격**: **MSA/DevOps 역량 전시용 포트폴리오** (실서비스 아님). 아키텍처 선택은 "워크로드 적합성"이 아니라 "역량 시연 효과"를 기준으로 판단한다. → §12 전시 전략 참조.
> **작성일 기준**: 2026-07-21

---

## 0. 핵심 결정: "스택을 통일해야 하는가?"

**아니오 — 런타임은 통일 불필요. 다만 "공유 방식"을 규율한다.**

MSA에서 각 서비스는 독립 배포 단위이며 네트워크(REST/메시지)로만 통신하므로, 서비스마다 다른 프레임워크·언어를 써도 된다(폴리글랏). 따라서 `budget-service`(Spring 5.3 / `javax`)와 신규 `portfolio-content-service`(Boot 3.2 / `jakarta`)가 **서로 다른 프로세스로 떠서 REST로 대화하는 것은 정상 동작**한다.

`javax` ↔ `jakarta` 비호환이 실제로 문제되는 지점은 **두 곳뿐**이다:

| 문제 발생 지점 | 왜 문제인가 | 이 설계의 대응 |
|---|---|---|
| **공유 라이브러리를 classpath로 함께 컴파일** | 하나의 JAR은 javax/jakarta 중 한쪽만 가능. javax로 빌드한 `JwtUtil`은 Boot3 서비스에서 로드 불가 | 코드 JAR 공유를 **최소화**. 공유는 **REST 계약(JSON)**으로. 불가피하면 순수 POJO(네임스페이스 의존 0) 유틸만 공유 |
| **서비스 간 코드 물리 복사** | 엔티티/서블릿/시큐리티 클래스가 네임스페이스 충돌 | 코드 복사 대신 **API 호출**로 대체 (openclaw가 이미 채택한 원칙) |

### 통합 3원칙
1. **공유하는 것은 코드(JAR)가 아니라 계약(REST/JSON)이다.** — 서비스 간 결합도 0 유지.
2. **각 서비스는 자기에게 맞는 스택으로 독립 배포한다.** — 신규/기능 중심 서비스는 Boot 3.2, 기존 openclaw 서비스는 현행 유지 후 여유 될 때 수렴.
3. **인증은 게이트웨이에서 1회 검증하고 신뢰 헤더로 전파한다.** — 각 서비스는 JWT 파싱을 하지 않고 `X-User-Id`만 신뢰(내부망 전제).

> 참고: "장기적으로 전부 Boot 3.2로 수렴"은 유지보수 관점의 **선택지**이지 통합의 **전제조건이 아니다**. 수렴은 서비스별로 개별 일정에 맞춰 진행한다.

---

## 1. 현행 자산 인벤토리

### 1.1 openclaw-msa (그릇 — 인프라·플랫폼)
- `openclaw-common`: 공유 JWT/DTO/Exception (BOM)
- `budget-service` (:8081): 가계부 + **인증 서버** + Spring Batch(메일 파싱)
- `blog-service` (:8082): **Tistory/Velog 자동발행 + 예약 스케줄러**
- `notification-service` (:8083): **Telegram 알림**
- 인프라: **Nginx 게이트웨이(auth_request)**, k8s 매니페스트, Jenkinsfile
- 스택: Spring Framework 5.3, `javax`, Boot 미사용, PostgreSQL 스키마 분리

### 1.2 portfolio (알맹이 — 기능·프론트)
- `dev-portfolio`: React 19 + TS + Vite + Tailwind (실 프론트엔드)
- `dev-portfolio-backend`: Spring Boot 3.2 모놀리식, `jakarta`, 60개 java
  - 도메인 엔티티: `User, Project, Career, Certification, Skill, Profile, DevNote, BlogPost, Contact, Visitor`
  - 부가: JWT 인증, RateLimiting, Swagger, Actuator/Prometheus, Flyway, Sentry, 파일 업로드
- `worklog`: 통합 무관 (작업로그 스크립트)

### 1.3 상호보완 관계 (합치려던 이유)
- 양쪽 **JWT 인증** 중복 → 단일 `auth-service`로 통합 근거
- portfolio엔 `BlogPost`/`DevNote` **엔티티만** 존재, openclaw엔 **자동발행 엔진** 존재 → 합치면 완결
- portfolio **Contact(문의 폼)** → openclaw **notification-service(Telegram)** 재사용 최적
- openclaw는 **게이트웨이·k8s·CI**, portfolio는 **기능·React** → 그릇 + 알맹이

---

## 2. 목표 아키텍처

```
                     [React 프론트 dev-portfolio]
                               │
                     [Nginx API Gateway]
                          │ auth_request
                          └──────────────▶ auth-service (/auth/verify)
          ┌──────────┬──────────┬──────────┬───────────┬──────────────┐
          ▼          ▼          ▼          ▼           ▼              ▼
     auth-service  portfolio   blog       contact    analytics    budget-service
     (User/JWT)    -content   -service   -service    -service     (가계부, 유지)
                   (Project    (BlogPost  (Contact)      (Visitor)      │
                    Career      DevNote       │                         ▼
                    Skill       +자동발행)     └──────▶ notification-service
                    Cert                                (Telegram) ← budget도 사용
                    Profile)
                          │
                  [PostgreSQL — 서비스별 스키마 분리]
        auth / portfolio / blog / contact / analytics / budget / notify
```

### 2.1 서비스 카탈로그

| 서비스 | 포트 | 책임 | 소스(portfolio에서 이동) | 재활용(openclaw) | 권장 스택 |
|---|---|---|---|---|---|
| `auth-service` | 8080 | 로그인·JWT 발급·검증, 사용자 | `User`, `AuthService`, `JwtUtil`, `SecurityConfig` | budget의 auth 흐름, common `JwtUtil` 개념 | Boot 3.2 |
| `portfolio-content-service` | 8084 | 포트폴리오 콘텐츠 CRUD | `Project, Career, Skill, Certification, Profile` | — | Boot 3.2 |
| `blog-service` | 8082 | 글 관리 + 외부 자동발행 | `BlogPost, DevNote` | **Tistory/Velog 발행 + 예약 스케줄러** | 택1 (§5 참고) |
| `contact-service` | 8085 | 문의 접수·조회 | `Contact` | — | Boot 3.2 |
| `analytics-service` | 8086 | 방문 기록·통계 | `Visitor`, `VisitorStats` | — | Boot 3.2 |
| `notification-service` | 8083 | Telegram 알림 | (Contact 트리거 연동) | **그대로 재사용** | 현행 유지 |
| `budget-service` | 8081 | 가계부(성격 상이) | — | **그대로 유지 또는 범위 제외** | 현행 유지 |
| `file-service`(선택) | 8087 | 파일 업로드/서빙 | `FileController` | — | Boot 3.2 |

> `budget-service`(가계부)는 portfolio와 도메인이 무관하다. **통합 산출물의 범위에 포함할지 결정 필요**(§11 오픈이슈). 포함해도 독립 서비스로 그대로 둔다.

---

## 3. 서비스 간 통신 계약 (코드 아닌 계약 공유)

### 3.1 인증 전파 (Nginx auth_request 재사용)
1. 프론트 → `POST /api/auth/login` → `auth-service`가 JWT 발급
2. 이후 모든 요청: Nginx가 `auth_request /auth/verify` → `auth-service`가 검증 후 `X-User-Id`(+ `X-User-Role`) 헤더 반환
3. Nginx가 하위 서비스로 `X-User-Id` 주입 → **하위 서비스는 JWT 파싱 불필요**, 헤더만 신뢰
4. 내부 서비스 간 호출(`contact → notification`)은 `/api/notify/`처럼 내부망 CIDR 화이트리스트

### 3.2 서비스 간 REST 계약 예시
- `contact-service` → `notification-service`: `POST /notify/send { "message": "새 문의: ..." }`
  (openclaw `NotificationClient` 패턴 그대로 — 실패해도 본 흐름 중단 금지)
- `blog-service` 예약 발행: 내부 스케줄러가 `BlogPost.status=SCHEDULED && scheduledAt<=now` 조회 → Tistory/Velog API 호출

### 3.3 공유 규약 (JAR 대신 문서/스키마)
- 표준 응답 포맷 `ApiResponse<T>` — 각 서비스가 **자기 스택으로 각자 구현**(복붙 아님, 스펙만 공유)
- 인증 헤더 규약(`X-User-Id`, `X-User-Role`) — 게이트웨이 계약 문서로 관리
- 불가피한 코드 공유는 **네임스페이스 의존 없는 순수 POJO/유틸**만 (예: 순수 문자열 JWT 파싱 헬퍼)

---

## 4. 데이터 설계 (PostgreSQL 스키마 분리)

단일 인스턴스 + 서비스별 스키마 (openclaw 방식 계승). Hibernate `default_schema`로 격리.

| 스키마 | 테이블 | 소유 서비스 |
|---|---|---|
| `auth` | users | auth-service |
| `portfolio` | projects, careers, skills, certifications, profile | portfolio-content-service |
| `blog` | blog_posts, dev_notes, posting_history | blog-service |
| `contact` | contacts | contact-service |
| `analytics` | visitors | analytics-service |
| `budget` | transactions, categories, budget_limits | budget-service |
| `notify` | (알림 로그, 선택) | notification-service |

**데이터 소유권 규칙**: 한 테이블은 한 서비스만 write. 타 서비스는 API로만 조회. 크로스 스키마 JOIN 금지(경계 침범).

---

## 5. blog-service 통합 — 가장 큰 스택 결정 지점

portfolio의 `BlogPost`/`DevNote`(jakarta) + openclaw `blog-service`의 자동발행(javax)을 합쳐야 한다. 두 방법:

- **5-A) openclaw blog-service(javax)에 엔티티만 재정의**: portfolio 엔티티 필드를 참고해 javax 기준으로 다시 선언. 자동발행 엔진은 그대로. → 발행 로직 재작성 없음, 엔티티만 이관.
- **5-B) blog-service를 Boot 3.2로 재작성**: portfolio 엔티티 그대로 가져오고, Tistory/Velog 클라이언트·스케줄러를 Boot 3로 포팅. → 코드량 있으나 스택 일관성↑, 이후 유지보수 유리.

> 권장: 자동발행 엔진이 blog-service의 핵심 가치이므로, **초기엔 5-A로 빠르게 붙이고**, 안정화 후 여유가 되면 5-B로 수렴.

---

## 6. 게이트웨이 라우팅 확장 (nginx-gateway.conf)

기존 `/api/budget/`, `/api/blog/` 패턴에 신규 서비스 라우트 추가:

```nginx
upstream auth      { server auth-service:8080; }
upstream portfolio { server portfolio-content-service:8084; }
upstream contact   { server contact-service:8085; }
upstream analytics { server analytics-service:8086; }

# 공개 엔드포인트 (검증 없이)
location /api/auth/login   { proxy_pass http://auth/auth/login; }
location /api/projects     { proxy_pass http://portfolio/projects; }   # 조회는 공개
location = /api/contacts   { proxy_pass http://contact/contacts; }     # 문의 제출 공개
location = /api/visitors   { proxy_pass http://analytics/visitors; }   # 방문 기록 공개

# 보호 엔드포인트 (auth_request 검증 후 X-User-Id 주입)
location /api/admin/ {
    auth_request     /auth/verify;
    auth_request_set $user_id $upstream_http_x_user_id;
    proxy_set_header X-User-Id $user_id;
    # 관리자 CRUD 라우팅...
}
```

프론트(React)는 Nginx가 정적 서빙 또는 별도 파드. portfolio README의 CORS(5173) 설정은 게이트웨이 단일 오리진으로 정리.

---

## 7. 단계별 마이그레이션 로드맵 (Strangler Fig)

모놀리식을 한 번에 쪼개지 않고, **게이트웨이 뒤에서 도메인별로 하나씩 떼어낸다.**

| 단계 | 작업 | 검증 |
|---|---|---|
| **Phase 0 — 준비** | 통합 리포 구조 확정, 게이트웨이에 portfolio 모놀리식을 통째로 라우팅(`/api/*`→portfolio-backend). 기존 기능 무손상 | 프론트 정상 동작 |
| **Phase 1 — 인증 분리** | `auth-service` 추출. 게이트웨이 auth_request를 auth-service로. 나머지 서비스는 헤더 신뢰로 전환 | 로그인/보호 API 동작 |
| **Phase 2 — 알림 연동** | `contact-service` 분리 + `notification-service`(재사용) 연결. 문의 시 Telegram 발송 | 문의→알림 수신 |
| **Phase 3 — 콘텐츠 분리** | `portfolio-content-service` 추출(Project/Career/Skill/Cert/Profile) | 포트폴리오 표시·관리 |
| **Phase 4 — 블로그 통합** | `blog-service`에 BlogPost/DevNote 이관 + 자동발행 결합(§5) | 예약 발행 동작 |
| **Phase 5 — 분석 분리** | `analytics-service`(Visitor) 추출 | 방문 통계 |
| **Phase 6 — 정리** | 모놀리식 잔여 제거, k8s/Jenkins 통합 파이프라인, 관측성(Actuator/Prometheus) 게이트웨이 통합 | 전체 E2E |

각 Phase는 **게이트웨이 라우트만 바꿔** 신규 서비스로 트래픽을 전환하므로, 문제 시 즉시 롤백(라우트 원복) 가능.

---

## 8. 리스크 & 완화

| 리스크 | 영향 | 완화 |
|---|---|---|
| javax/jakarta 혼재 | 코드 공유 시 컴파일 실패 | 공유는 REST 계약으로. 코드 JAR 공유 금지(§0) |
| 분산 트랜잭션 | 서비스 경계 넘는 정합성 | 경계 설계상 최소화. 알림 등은 최종적 일관성 허용(실패 무시) |
| 인증 헤더 위조 | 내부 서비스가 X-User-Id 맹신 | 하위 서비스 포트를 외부 비노출, 내부망 CIDR 화이트리스트(openclaw 기존 방식) |
| 로컬 개발 복잡도↑ | 서비스 6~8개 동시 구동 | docker-compose 통합, 개발 시 필요한 서비스만 기동 |
| budget 도메인 이질성 | 포트폴리오와 무관한 코드 상존 | 범위 결정(§11). 포함 시 독립 서비스로 격리 |

---

## 9. 저장소/빌드 구조 (제안)

폴리글랏이므로 부모 BOM 강제는 하지 않고, 서비스별 독립 pom 허용.

```
openclaw-msa/                 # 통합 루트 (인프라 + 서비스 오케스트레이션)
├── gateway/                  # nginx-gateway.conf
├── auth-service/             # Boot 3.2
├── portfolio-content-service/# Boot 3.2
├── blog-service/             # (택1) 5.3 유지 or Boot 3.2
├── contact-service/          # Boot 3.2
├── analytics-service/        # Boot 3.2
├── notification-service/     # 현행 5.3 유지
├── budget-service/           # 현행 5.3 유지 (범위 결정)
├── frontend/                 # dev-portfolio (React) 이관
├── k8s/                      # 통합 매니페스트
├── docker-compose.yml        # 로컬 통합 기동
└── Jenkinsfile               # 서비스별 parallel 빌드
```

> ⚠️ 선행 정리: 현재 `C:\openclaw-msa\src/` 는 모듈화 이전 **중복 원본(빌드 안 됨)** → 통합 착수 전 삭제 권장.

---

## 10. 프론트엔드(React) 처리

- `dev-portfolio`의 `lib/api.ts` 호출 경로를 **게이트웨이 단일 오리진**(`/api/*`)으로 통일 → 서비스 분리를 프론트가 인지하지 않도록 은닉.
- 빌드 산출물을 Nginx가 정적 서빙하거나 별도 파드로 배포.
- 서비스가 쪼개져도 프론트 코드 변경 최소화(게이트웨이가 흡수).

---

## 11. 결정이 필요한 오픈 이슈

1. **budget-service(가계부)를 통합 산출물에 포함?** — 도메인 무관. (a) 포함해 독립 서비스 유지 / (b) 범위 제외.
2. **blog-service 스택** — §5의 5-A(빠른 결합) vs 5-B(Boot로 재작성).
3. **file-service 분리 여부** — 파일 업로드를 독립 서비스로 뺄지, portfolio-content-service에 둘지.
4. **관측성/부가기능**(Sentry, Prometheus, Flyway, RateLimiting) — 어느 서비스까지 적용할지.
5. **프론트 배포 형태** — Nginx 정적 서빙 vs 별도 파드.

---

## 12. 전시 전략 (Showcase Strategy) — 이 프로젝트의 최우선 관점

이 시스템의 목적은 실서비스 운영이 아니라 **MSA/DevOps 역량을 채용담당자에게 증명**하는 것이다. 따라서 비즈니스 로직(포트폴리오 CRUD)은 조연이고, **플랫폼·운영 역량이 주연**이다. 아래 우선순위로 투자한다.

### 12.1 냉정한 전제
> 이 워크로드(1인 개발·저트래픽·개인 포트폴리오)에는 **모듈러 모놀리스가 실용적으로 더 적합**하다. 그럼에도 MSA 운영 역량 시연을 위해 **의도적으로** MSA를 채택한다. 이 판단 자체를 README·면접에서 명시하는 것이 핵심 차별화 포인트다("언제 MSA를 쓰지 말아야 하는지 아는 것"이 시니어 신호).

### 12.2 점수 따는 항목 (투자 우선순위)
| 우선 | 항목 | 왜 중요 | 현재 자산 |
|---|---|---|---|
| ★★★ | **관측성 3종** — 메트릭(Prometheus+Grafana) / 분산 트레이싱(Zipkin·Tempo) / 중앙 로깅(Loki·ELK) | MSA 포트폴리오 당락 지점. "서비스 간 흐름을 추적할 줄 안다" 증명 | portfolio에 Actuator·Micrometer·Prometheus 이미 존재 → 절반 완료 |
| ★★★ | **API 게이트웨이 + 중앙 인증** | MSA 필수 관문 | openclaw Nginx auth_request 존재 (Spring Cloud Gateway로 승격 시 어필↑) |
| ★★★ | **CI/CD 파이프라인 + K8s** | DevOps 핵심 | openclaw Jenkinsfile·k8s 존재 → probe/HPA/ConfigMap/Secret 보강 |
| ★★☆ | **회복탄력성** — 타임아웃·재시도·서킷브레이커(Resilience4j) | 분산 시스템 장애 대응 이해 증명 | 신규 추가 |
| ★★☆ | **비동기 이벤트** — 메시지 브로커(Kafka·RabbitMQ)로 Contact→알림 등 | 동기 결합 탈피, 이벤트 기반 아키텍처 시연 | 신규 (§12.4) |
| ★☆☆ | 서비스 디스커버리(Eureka/Consul) | 규모상 필수는 아니나 시연 가치 | 선택 |
| ★☆☆ | 아키텍처 다이어그램·ADR 문서 | 스토리텔링 | 신규 |

### 12.3 반드시 피할 함정 (주니어 신호)
- **분산 모놀리스**: 서비스가 서로의 DB를 직접 조회하거나 동기 호출로 강결합 → MSA 최악 안티패턴. §4 "데이터 소유권 규칙" 엄수.
- **MSA-in-name-only**: 게이트웨이·트레이싱·CI 없이 프로세스만 여러 개.
- **스택 메시지 충돌**: openclaw의 "Spring Boot 미사용(프레임워크 내부 학습)"은 *DevOps 전시* 목적과 상충. **전시 목적이면 Boot 3.2로 통일**해 메시지를 일관화 권장(→ §0·§11-2 재검토).

### 12.4 전시 효과를 위한 아키텍처 상향 (선택적 확장)
- **이벤트 기반 전환**: `contact → notification`을 REST 동기 대신 **메시지 브로커 발행/구독**으로 → 이벤트 아키텍처 역량 시연.
- **분산 트레이싱**: 모든 서비스에 traceId 전파(W3C Trace Context) + Zipkin/Tempo 대시보드 스크린샷을 README에.
- **Grafana 대시보드**: 서비스별 지연·에러율 패널을 README 상단에 배치 → 시각적 임팩트.

### 12.5 스택 방향 재확정 (전시 관점)
전시 목적에서는 **§0의 폴리글랏 허용보다 "Boot 3.2 단일 스택 + 관측성/게이트웨이 강화"가 메시지상 유리**하다. 폴리글랏은 그 자체가 목적일 때만 어필되며, 여기서는 운영 성숙도를 보여주는 것이 우선이므로 스택 일관성을 택한다. (openclaw 서비스는 Boot 3.2로 수렴)

---

## 부록 A. portfolio 엔티티 → 서비스 매핑 요약

| 엔티티 | 이동 대상 서비스 |
|---|---|
| User | auth-service |
| Project, Career, Skill, Certification, Profile | portfolio-content-service |
| BlogPost, DevNote | blog-service |
| Contact | contact-service |
| Visitor | analytics-service |
