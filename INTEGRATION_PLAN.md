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

## 5. 로드맵 (Strangler Fig — 태스크와 1:1, 플랫폼 관점 반영)

| Phase | 내용 |
|---|---|
| **0** | 플랫폼 레이아웃 확정(platform/apps/infra), 중복 `src/` 정리, 게이트웨이에 모놀리식 통째 라우팅 |
| **1** | platform/auth 분리 (인증 중앙화) |
| **2** | apps/portfolio/contact + platform 알림 연동 (여기서 **messaging 백본** 첫 도입) |
| **3** | apps/portfolio/content 분리 |
| **4** | apps/blog 통합 (엔티티 + 자동발행) — **앱 추가 확장성 첫 시연** |
| **5** | apps/portfolio/analytics 분리 + **apps/budget 편입** (2번째 도메인 = 확장성 증거) |
| **6** | 관측성·트레이싱·회복탄력성 + infra(멀티환경/GitOps) + 전환/확장 스토리 (면접 방어) |

---

## 6. 지금 상태 & 다음

- [x] 전체 지도 + 주석 컨벤션 + 스캐폴딩 (1차)
- [x] **플랫폼으로 격상 + 확장성 5축 반영** (2차, 이번 단계)
- [ ] 미해결 결정: **사용자 연차**(문서 톤), **blog 스택(5-A/5-B)** → `MIGRATION_DESIGN §11`
- [ ] 다음: Phase 0 — `platform/`·`apps/`·`infra/` 물리 레이아웃 정리 + 중복 `src/` 삭제 + `platform/gateway` `@DEEP`부터

> ⚠️ 선행 정리: 루트 `src/` 는 모듈화 이전 **중복 원본(빌드 안 됨)** → Phase 0에서 삭제.
