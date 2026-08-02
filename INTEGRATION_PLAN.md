# INTEGRATION_PLAN — 통합 마스터 플랜 (스캐폴딩 지도)

> 이 문서는 **전체 지도**다. 상세 설계는 `MIGRATION_DESIGN.md`, 확장성은 `docs/scalability.md`, 실제 채움은 각 위치의 스텁/주석을 따라간다.
> 작업 방식: **전체 뼈대 먼저 → 주석 마커 따라 부분부분 딥하게 채움 → 지점끼리 연결**. (상세 X, 전체 O로 시작)

---

## 0. 이 프로젝트가 뭐고 왜 하는가 (한 눈에)

- **정체**: 모놀리식 실무 개발자가 "MSA 경험자"를 뽑는 곳으로 이직하기 위한 **역량 전시 포트폴리오**.
- **★ 격상된 프레임**: "포트폴리오 사이트 하나"가 아니라 **여러 독립 앱을 얹는 "개인 MSA 플랫폼"**이다.
  - 확장성이 설계의 1급 목표. 서비스 6개에서 끝나지 않고 **앱을 계속 추가할 수 있는 골격**을 만든다.
- **최강 카드**: "왜 쪼개는지 · 언제 쪼개면 안 되는지 · **어떻게 확장 가능하게 만드는지**까지 아는 사람".

### 자산 지도
| 위치 | 정체 | 플랫폼에서의 역할 |
|---|---|---|
| `C:\dev\portfolio` (= K-portfolio) | Boot 3.2 모놀리식 + React + 충실한 K8s/CI | **앱 #1 (portfolio)** + 인프라 원천 |
| `C:\openclaw-msa` (여기) | MSA 실험작 + Nginx 게이트웨이 | **플랫폼 루트** + 앱 #2/#3 원천 |
| openclaw budget(가계부) | 이질 도메인 | **앱 #2 (budget)** = "새 도메인도 수용" 확장성 증거 ★ |
| openclaw blog 자동발행 | 스케줄러+외부API | **앱 #3 (blog)** |

> 핵심 전환: budget이 "도메인 안 맞아 애매"했던 게, 플랫폼 관점에선 **서로 다른 도메인도 얹히는 확장성의 증거**로 뒤집힌다.

---

## 1. 주석 마커 컨벤션 ★ (전체 공통 규약 — 변경 없음)

```
@PLAN     이 자리에서 무엇을 할 것인가 (한 줄 목표)
@FROM     소스 — 어디서 가져오는가
@HOW      어떻게 적용하는가 (이관/재작성/API호출/재사용)
@PHASE:n  로드맵 몇 단계에 속하는가 (0~6)
@SCALE    이 지점이 어느 확장성 축에 기여하는가 (docs/scalability.md 참조) ★신규
@DEEP     ⬅ 나중에 상세 주석/구현을 채울 자리 (지금은 비워둠)
@LINK     연결되는 다른 지점 (파일경로#마커) — 지점끼리 잇는 실
@RISK     주의점/함정 (선택)
```

> 진행 규칙: `@DEEP`가 남은 지점은 "미완성". `grep -rn "@DEEP" .` 로 추적. 채우면 `@LINK`를 양방향 갱신해 연결.

---

## 2. ★ 격상된 구조 — platform(공유 골격) + apps(개별 앱) + infra(환경/배포)

기존 `services/` 평면 나열을 아래처럼 재구성한다. **platform은 안정적으로, apps는 계속 늘리는** 구조.

```
openclaw-msa/  (플랫폼 루트)
├── platform/           ← 앱들이 공유하는 공통 계층 (확장의 척추)
│   ├── gateway/            단일 진입점·라우팅·인증위임        [기존 gateway/]
│   ├── auth/               중앙 인증 (JWT 발급/검증)         [기존 services/auth-service/]
│   ├── observability/      관측·트레이싱·회복탄력성           [기존 observability/]
│   ├── messaging/          ★ 이벤트 브로커 (비동기 백본)      [신규]
│   └── service-template/   ★ 새 서비스 찍어내는 골격          [신규]
│
├── apps/               ← 플랫폼 위의 독립 앱들 (계속 추가 가능)
│   ├── portfolio/          앱 #1 = K-portfolio 분해
│   │   ├── content-service/   Project/Career/Skill/Cert/Profile
│   │   ├── contact-service/   문의 + 알림 연동
│   │   └── analytics-service/ 방문 통계
│   ├── budget/             앱 #2 = 가계부 (확장성 증거)       [openclaw budget-service]
│   └── blog/               앱 #3 = 블로그 자동발행            [openclaw blog-service]
│
├── infra/              ← 환경·배포 확장                       [신규]
│   ├── helm/  terraform/  environments(dev/stg/prod)/  argocd/
│
└── docs/               ← 설계·결정·서사
    ├── adr/  interview-narrative.md  scalability.md ★
```

> ⚠️ **물리 이동은 Phase 0에서 점진 수행.** 지금은 지도상 재배치 + 주석 마커. 기존에 만든 `services/*`·`gateway/`·`observability/` 스텁은 위 `platform/`·`apps/portfolio/` 소속으로 **재분류됨**(각 스텁이 신규 위치 스텁과 @LINK로 연결).

---

## 3. 확장성 5축 (docs/scalability.md 에서 상세)

| 축 | 확장 대상 | 핵심 기법 | 관련 위치 |
|---|---|---|---|
| **서비스 확장** | 새 앱/서비스 추가 | service-template, 공통 라이브러리, OpenAPI 계약 | `platform/service-template/` |
| **트래픽 확장** | 부하 증가 | Stateless + HPA + LB, 부하테스트(k6) | `infra/`, `platform/observability/` |
| **데이터 확장** | 데이터·읽기 증가 | 캐시(Redis), DB per service, 읽기복제, CQRS | 각 앱, `MIGRATION_DESIGN §4` |
| **통신 확장** | 결합도 낮추기 | 동기 REST → 이벤트(Kafka/RabbitMQ) | `platform/messaging/` |
| **환경/배포 확장** | dev→prod, 반복 배포 | 멀티환경, Helm, GitOps(ArgoCD), IaC(Terraform) | `infra/` |

> ⚖️ 균형: **확장 가능하게 "설계"하되, 구현은 앱 1~2개 + 확장 포인트 골격으로 증명**. 다 벌려놓고 미완성이면 오히려 감점.

---

## 4. 스캐폴딩 파일 인덱스

| 파일 | 영역 | 상태 |
|---|---|---|
| `INTEGRATION_PLAN.md` | 마스터 지도 | (이 문서) |
| `MIGRATION_DESIGN.md` | 상세 설계서 | 기존 |
| `docs/scalability.md` | ★ 확장성 5축 | 신규 |
| `docs/adr/0001-...md` | 결정 기록 | 기존 |
| `docs/interview-narrative.md` | 면접 방어 | 기존 |
| `platform/gateway/PLAN.md` | 게이트웨이 | 기존(재분류) |
| `platform/service-template/PLAN.md` | ★ 서비스 골격 | 신규 |
| `platform/messaging/PLAN.md` | ★ 이벤트 백본 | 신규 |
| `apps/README.md` | ★ 앱 카탈로그 | 신규 |
| `infra/PLAN.md` | ★ 환경/배포 확장 | 신규 |
| `services/auth-service/PLAN.md` → `platform/auth/` | 인증 | 기존(이동 예정) |
| `services/portfolio-content-service/`, `contact-service/`, `analytics-service/` → `apps/portfolio/` | 앱#1 | 기존(이동 예정) |
| `observability/PLAN.md` → `platform/observability/` | 관측성 | 기존(이동 예정) |

---

## 5. 로드맵 (Strangler Fig — 상태 실시간 반영)

> 상태 범례: ✅ 완료 · 🔵 진행 중 · ⬜ 예정 · (날짜)는 완료/착수일

| Phase | 내용 | 상태 |
|---|---|---|
| **0** | 플랫폼 레이아웃 확정(platform/apps/infra) + K-portfolio As-Is 흡수 | ✅ 완료 (07-24) |
| **1** | platform/auth 분리 (인증 중앙화) + 게이트웨이 라우팅 | ✅ 코드 완료 (07-24) · 도커 실행검증만 보류 |
| **2** | apps/portfolio/contact + 알림 연동 | ✅ 코드 완료 (07-24) — contact-service + notification-service(REST) + compose·게이트웨이 통합. 이벤트(messaging) 전환은 Phase 6, 도커 실행검증 보류 |
| **3** | apps/portfolio/content 분리 | ✅ 코드 완료 (08-02) — content-service 기동 검증(projects/profile API 정상) |
| **4** | apps/blog 통합 (엔티티 + 자동발행) — 앱 추가 확장성 첫 시연 | 🔵 착수 (08-02) — blog-service 코드 완성, dry-run 모드(실 API @DEEP) |
| **5** | apps/portfolio/analytics 분리 + apps/budget 편입 (2번째 도메인 = 확장성 증거) | 🔵 착수 (08-02) — analytics-service 코드 완성, budget 편입은 @DEEP |
| **6** | 관측성·트레이싱·회복탄력성 + 부하테스트 + infra + 전환/확장 스토리 | 🔵 착수 (07-24) — 부하테스트(k6) + 관측성(Prometheus+Grafana compose 통합) 작성. 실행측정·분산트레이싱·회복탄력성(Resilience4j)은 다음 |

### Phase 6 세부 진행 (착수 — 대용량/관측성 실탄)
| 항목 | 상태 |
|---|---|
| k6 부하테스트 시나리오 (`infra/load-test/auth-login.js`) | ✅ 작성 완료 (07-24) — 로그인 API, VU 계단(20→100), SLO(p95<500ms, 에러율<1%) |
| 측정표 템플릿 (`infra/load-test/README.md`) | ✅ 작성 완료 (07-24) — HPA 전/후 비교 자리(@DEEP) |
| Prometheus 스크레이프 설정 (`platform/observability/prometheus.yml`) | ✅ 작성 완료 (07-24) — auth/contact/notification job. monolith는 prod에서 prometheus 미노출(@DEEP) |
| compose에 prometheus(9090)+grafana(3000) 통합 | ✅ 완료 (07-24) |
| **실측정 실행** (`docker compose up` → `k6 run`) | ⬜ **다음 세션 최우선** — 도커 보류 풀리면 바로 |
| Grafana 대시보드 패널 구성(서비스별 지연/에러율) | ⬜ 예정 |
| 분산 트레이싱 (Zipkin/Tempo, traceId 전파) | ⬜ 예정 |
| 회복탄력성 (Resilience4j: 타임아웃/재시도/서킷브레이커, contact→notification에 적용) | ⬜ 예정 |
| 이벤트 기반 전환 (contact→notification REST를 messaging으로) | ⬜ 예정 (platform/messaging/PLAN.md 참조) |

### Phase 1 세부 진행 (진행 중이라 쪼개서 추적)
| 항목 | 상태 |
|---|---|
| auth-service 코드 (로그인·JWT 발급·`/auth/verify` 검증) | ✅ 완료 (07-24) |
| 실물 PostgreSQL 설계 (docker-compose + auth 스키마) | ✅ 완료 (07-24) |
| auth-service 컨테이너화 (Dockerfile + compose 통합) | ✅ 완료 (07-24) |
| 도커 실행 검증 (`docker compose up`) | ⏸ 보류 (사용자: 나중) |
| 게이트웨이 라우팅 conf (Nginx → monolith + /auth→auth-service + auth_request 검증 위임) | ✅ 완료 (07-24) |
| monolith 컨테이너 통합 (K-portfolio prod → kmsa postgres, public 스키마 임시) | ✅ 완료 (07-24) |
| compose 전체 스택 통합 (postgres+auth+monolith+gateway 한 번에 기동) | ✅ 완료 (07-24) · 실행 검증만 보류 |
| monolith에서 auth 코드 제거 (Strangler 분리 완료) | ✅ 완료 (07-24) — JWT필터→헤더신뢰필터 교체, AuthController/AuthService/JwtUtil/JwtAuthenticationFilter 삭제 |

---

## 6. 진행 현황 (실시간 — 매 작업마다 갱신)

### ✅ 완료

**2026-07-24 세션:**
- 통합 리포 생성 + 격상 구조(platform/apps/infra) + 계획/스캐폴딩
- **Phase 0**: K-portfolio As-Is 모놀리스 흡수 → `apps/portfolio/monolith/`
- **Phase 1**: auth-service 전체 코드 완료 + monolith에서 auth 제거(Strangler 완결)
- **Phase 2**: contact-service + notification-service 코드 완료, compose·게이트웨이 통합
- **Phase 6 착수**: k6 부하테스트 시나리오 + Prometheus/Grafana compose 통합

**2026-08-02 세션:**
- notification-service/Dockerfile + contact-service/Dockerfile 생성 (Phase 2 누락 파일)
- gateway 포트 80→8090 변경 (Caddy(k-devops.duckdns.org) 포트 충돌 해소)
- **k6 실측정** — auth-service 직접(8081), TPS 44.7, p95 744ms, 에러율 0.00% (8,081건)
- init-db portfolio 스키마 추가, Prometheus content-service job 추가
- **Phase 3**: content-service 완전 구현 + 기동 검증 (projects/profile API 정상)
  - 엔티티 5종 + Repository + Service + Controller + DataInitializer + Dockerfile
  - compose + nginx 라우팅 통합
- compose `name:` 추가 (k-msa-platform / openclaw-msa 프로젝트명 충돌 방지)

### ⬜ 다음 세션 시작점 (git pull 받고 바로 여기부터)
**최우선 — Phase 6 실탄 완성 (자소서 대용량 갭을 메우는 작업)**:
1. **도커 실행 검증** (지금까지 보류 중) — `cd infra && docker compose up -d --build` → 8개 컨테이너(postgres/auth/monolith/gateway/contact/notification/prometheus/grafana) 정상 기동 확인
2. **k6 실측정** — `k6 run infra/load-test/auth-login.js` 실행 → `infra/load-test/README.md`의 측정표에 실제 TPS/p95/에러율 기록
3. **Grafana 대시보드** — localhost:3000(admin/admin) 접속, Prometheus 데이터소스 연결, 서비스별 패널 구성 → 스크린샷 확보
4. **분산 트레이싱**(Zipkin) — `platform/observability/PLAN.md` 참고해 착수
5. **회복탄력성**(Resilience4j) — contact→notification 호출에 타임아웃/서킷브레이커 적용

**그다음**: Phase 3(content 분리) → Phase 4(blog) → Phase 5(analytics+budget)

### ⚠️ 계획 대비 변경·추가 이력 (Changelog — "왜 바뀌었나")
- **07-23** 통합 위치 = **새 리포 `k-msa-platform`**로 확정 (openclaw 승격/K-portfolio 확장 아님). 이유: 전환 과정을 커밋 히스토리로 남기려고.
- **07-24** DB 설계를 **H2 → 실물 PostgreSQL**로 전환. 원계획엔 없던 결정. 이유: dev-prod parity + JD의 RDB/PostgreSQL 요구 + 이력 임팩트 (사용자 제안).
- **07-24** **auth 컨테이너화(Dockerfile+compose)를 Phase 1로 당김**. 원래 infra/Phase 6 성격이나 "실물 기동"을 조기 확보하려고.
- **07-24** 도커 실행 검증은 **보류**(사용자: 나중에) — 다음 세션 최우선 항목으로 이월.
- **07-24** **Phase 6(부하테스트+관측성)을 Phase 3~5보다 먼저 착수.** 원래 순서상 나중이나, "대용량 트래픽" JD 갭을 메우는 게 이직 관점에서 가장 시급하다고 판단해 순서를 앞당김. (자소서 §7 참조)

### 미해결 결정
- blog 스택 5-A/5-B (폴리글랏 방침이라 둘 다 가능). budget 편입 범위. 분산 트레이싱 도구(Zipkin vs Tempo).

> 📌 규칙: 이 문서(§5 로드맵 상태 + §6 진행현황/Changelog)는 **작업할 때마다 실시간 갱신**한다.
>   - 착수 시 해당 항목 🔵, 완료 시 ✅(날짜), 계획에 없던 게 생기면 Changelog에 "왜"와 함께 기록.

---

## 7. 자기소개서 진행 상황 (네이버웹툰 백엔드 경력 지원)

> ⚠️ 실제 자소서 파일은 개인정보라 이 리포(public) 밖, **`C:\dev\naver-webtoon-자기소개서.txt`** 에 있음.
>   이 섹션은 "무엇을 왜 그렇게 썼는지"의 진행 기록만 남긴다(내용 자체는 파일 참조).

### 상태: 5문항 전체 초안 완성 → 피드백 반영 → **제출 가능 수준** (2026-07-24 기준)

| 문항 | 상태 | 핵심 |
|---|---|---|
| 1번 (지원동기) | ✅ 완성 | "기능→운영" 각성 서사 + 개인 기질("원인 확인 전 못 넘어감") + 안정성/확장성 마무리 |
| 2번 (강점/시너지) | ✅ 완성 (최강 문항) | 실무 정량: 40만건 규모 / SAP 10종 / 0.05초 복구 / DB이관 1000여건 4중검증 / 디스크78%회복 / 배치 |
| 3번 (글로벌 관심) | ✅ 완성 | 외부연계 환경차 → 글로벌 유추 + JD(법령·컴플라이언스·결제) 직결 마무리 |
| 4번 (협업) | ✅ 완성 (거의 무수정) | SGI 다자협업, "누가 맞나가 아니라 무엇이 사실인가" |
| 5번 (포폴, 선택) | ✅ 완성 | "왜 만들었는지" 중심, 사실 정정 반영 |

### 전략 원칙 (왜 이렇게 썼는지 — 다음에 다른 자소서 쓸 때도 적용)
1. **프로젝트 자랑이 아니라 사고방식 중심.** 경력직은 "뭘 했나"보다 "어떻게 사고하나"를 봄.
2. **문항별 역할 분담, 반복 제거.** 1=왜/2=강점/3=글로벌/4=협업/5=포폴이 겹치지 않게.
3. **사실만 쓴다 — 검증 가능해야 함.** 프로젝트 코드와 대조해 허위 제거(아래 §7-1).
4. **약점을 먼저 말하지 않는다.** "대용량 아직 못했다"가 아니라 "부하테스트로 학습 중"처럼 긍정 프레이밍(사실은 유지, 순서만 긍정 우선).

### §7-1. 사실 검증에서 잡아낸 허위/오류 (반드시 기억 — 재발 방지)
- ❌ "Service Discovery 구성" — openclaw엔 Eureka/Consul 없음 → ✅ "서비스 이름(DNS) 기반 탐색"으로 정정
- ❌ "Spring Boot Auto Configuration 이해"(OpenClaw 맥락) — openclaw는 **Spring Boot 아님**(Framework 5.3) → ✅ "자동 구성 없이 직접 배선"으로 정정
- ❌ "대용량 트래픽 처리 경험" — 근거 없음(개인 프로젝트는 본인이 "저트래픽" 명시, 작업일지엔 TPS/동시접속 기록 없음) → ✅ "약 40만 건 대량 데이터", "SAP 10종 연계"처럼 **실제로 있는 규모**로 대체
- ❌ 다국어 시스템(엠로 SRM) → "해외/글로벌 경험"으로 연결 — **오연결**(다국어는 확장성 설계이지 해외 서비스 운영이 아님) → 3번에서 제거
- ⚠️ K-portfolio `DataInitializer`의 "XYZ 스타트업 주니어" 등 **템플릿 잔재 문구** — 실제 경력 아님, 첨부 전 확인(사용자 확인: 이미 정리됨)

### §7-2. 다음에 할 일 (자소서 관점)
- 프로젝트 §6 "다음 세션 시작점"의 **부하테스트 실측정**이 완료되면 → 2번/5번의 "부하 테스트로 학습 중" 표현을 **"부하 테스트로 초당 N건 처리 검증"** 실측 수치로 업그레이드 가능 (JD 최대 갭 해소)
- 국내 대비 KREAM Pay 등 **다른 회사 자소서**도 동일 원칙(§7 전략 원칙)으로 별도 작성 예정이었음 — 미착수
- 경력기술서 별도 작성 여부 미정
