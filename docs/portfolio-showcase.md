# K-portfolio 소개 설명서 (남에게 "이건 이거고, 왜 이렇게 했다"를 말하기 위한 근거 자료)

> 대상: `C:\dev\portfolio` (github.com/2kijung/K-portfolio) — Spring Boot 3.2 모놀리식 + React 19 풀스택 포트폴리오.
> 이 문서는 실제 코드 근거로 작성됐다. **과장 없이 정확하게** 설명하는 것이 목표 — 면접에서 "이건 왜 이렇게 했나"에 근거로 답할 수 있게.

---

## 0. 한 줄 정체성 (먼저 이렇게 말하라)

> **"겉은 개발자 포트폴리오 사이트지만, 실제 목적은 '내가 백엔드·DevOps를 이렇게 다룰 수 있다'를 증명하는 것입니다. 프론트는 콘텐츠를 보여주는 얇은 렌더러이고, 진짜 무게중심은 Spring Boot 백엔드와 K8s 인프라에 있습니다."**

근거: Hero 섹션의 역할 텍스트가 "Backend / MSA / DevOps / K8s"이고, 홈 화면에 **DevOps 트러블슈팅 사례를 데이터로 박제한 섹션**이 따로 있음. 프론트엔드는 페이지가 4개뿐(홈/로그인/관리자/404)인 반면, 백엔드·인프라는 프로덕션 패턴을 촘촘히 구현.

---

## 1. 전체 아키텍처 (이렇게 그려서 설명하라)

```
[방문자] → Ingress(nginx) → Frontend(React 빌드물을 Nginx가 서빙, /api는 백엔드로 프록시)
                                   → Backend(Spring Boot :8080 /api) → PostgreSQL(StatefulSet)
[관리자] → /admin (CMS) → 백엔드 인증 API로 콘텐츠 CRUD
[운영]   Jenkins(pollSCM 5분) → 빌드 → 이미지 → minikube load → kubectl apply → 헬스체크
        Prometheus + Grafana → 백엔드 /actuator/prometheus 스크레이프
```

**한 문장 설명**: "로컬 Minikube 한 대 위에, 실제 프로덕션 K8s의 안정성·보안·관측성·CI/CD 패턴을 최대한 재현한 풀스택 + 홈랩 인프라입니다."

**핵심 설계 철학 (이게 이 프로젝트의 정체성)**:
> **"콘텐츠는 코드에 하드코딩하지 않고 전부 DB에 넣고, 관리자 CMS로 관리한다."**
> 홈 화면 각 섹션(프로필/경력/스킬/프로젝트/개발노트)이 하드코딩이 아니라 백엔드 DB에서 온다.
> → "코드 배포 없이 내용만 갱신"이 가능. 포트폴리오를 계속 업데이트해야 하는 개발자에게 합리적인 선택.
> 즉 **프론트 = 렌더러, 진실의 원천 = Spring Boot + DB.**

---

## 2. 프론트엔드 — "이건 이거고, 왜 이렇게 했다"

**스택**: React 19 + TypeScript + Vite + Tailwind CSS 4 + wouter(라우팅) + shadcn/ui.

| 무엇을 | 어떻게 | 왜 (이렇게 말하라) |
|---|---|---|
| 페이지 4개 | 홈(원페이지 스크롤 10섹션)/로그인/관리자/404 | "콘텐츠가 전부 DB라 상세 페이지를 늘릴 필요가 없었다. 개인 포트폴리오에 맞춘 최소 라우팅" |
| wouter (react-router 아님) | 초경량 라우터 ~2KB | "라우트 5개 SPA에 react-router는 과하다. 번들 경량화를 택했다" |
| 관리자 CMS (876줄 단일 페이지) | 8개 탭으로 모든 콘텐츠 CRUD | "1인 관리자 화면이라 컴포넌트 분리보다 한 파일 응집을 택했고, 도메인마다 동일 CRUD 패턴이라 인지 부하가 낮다" |
| 커스텀 디자인 시스템 | oklch 색공간 + CSS 변수 토큰, 글래스모피즘/네온 다크 테마 | "Tailwind v4의 CSS-first 방식으로 '딥테크' 톤을 일관되게. 다크 단일 테마라 토글은 뺐다" |
| 애니메이션 직접 구현 | IntersectionObserver(useInView) + CSS keyframe + Canvas 파티클 | "framer-motion 없이 직접 만들었다 — 필요한 효과가 단순했고, 원리를 이해하고 통제하려고" |

### ⚠️ 프론트엔드에서 반드시 정직하게 구분할 것 (Manus 스캐폴드 잔재)
이 프로젝트는 **Manus AI 스캐폴드에서 출발**했다. 그래서 `package.json`에 있지만 **실제로는 안 쓰는 라이브러리가 많다.** 남에게 "이걸 썼다"고 말하면 안 되는 것들:
- **axios** — 선언돼 있지만 미사용. 실제로는 브라우저 `fetch`로 직접 구현.
- **framer-motion** — 미사용(0건). 애니메이션은 CSS + IntersectionObserver.
- **react-hook-form / zod** — 미사용. 폼은 전부 `useState`.
- **next-themes** — 앱 테마 아님(자체 ThemeContext 사용). shadcn sonner 스캐폴드에만 존재.
- **shadcn/ui 60개 중 실사용 7개**(button/card/input/tabs/dialog/tooltip/sonner). 나머지는 프리셋 잔재.

→ 소개 시: **"스캐폴드 기반이라 미사용 의존성이 있고, 실제 구현은 fetch·useState·직접 만든 훅으로 했다"**고 말하면 정확하고 오히려 코드를 정확히 파악하고 있다는 인상을 준다.

---

## 3. 백엔드 — "이건 이거고, 왜 이렇게 했다" (여기가 무게중심)

**스택**: Spring Boot 3.2 / JDK 17 / Spring Security + JWT / JPA / H2(개발)·PostgreSQL(운영).
**구조**: `Controller → Service → Repository → Entity` 전형적 4계층 레이어드 모놀리식 + DTO 경계.

### 3.1 도메인 (10개 엔티티)
User / Project / Career / Certification / Skill / Profile / DevNote / BlogPost / Contact / Visitor.
- **관계는 딱 하나** (BlogPost → User). 나머지는 독립 테이블.
- 왜: "각 섹션이 독립적으로 CRUD되는 콘텐츠라 조인이 불필요했다. 화면 섹션 = 테이블로 매핑한 콘텐츠 저장소 성격."
- `displayOrder` 필드 반복 → "관리자가 프론트 표시 순서를 제어하려고."

### 3.2 인증/보안 (설명하기 좋은 지점)
| 무엇을 | 어떻게 | 왜 |
|---|---|---|
| JWT 인증 | 로그인 → HS256 JWT 발급 → `Authorization: Bearer` 헤더, OncePerRequestFilter로 매 요청 검증 | "무상태 인증으로 서버 세션 부담 제거" |
| BCrypt | 비밀번호 해시 저장 | "평문 저장 금지, 기본 보안" |
| 레이트리미팅 | Guava RateLimiter, IP당 초당 100요청, 초과 시 429, X-Forwarded-For 처리 | "공개 POST(문의·방문기록)가 있어 스팸/무차별 요청 방어. 프록시 뒤 배포를 전제로 X-Forwarded-For를 봤다" |
| 읽기 공개 / 쓰기 인증 | SecurityConfig가 URL+메서드로 일괄 결정 | "방문자는 GET으로 다 보고, 생성·수정·삭제와 관리 조회만 인증" |

### 3.3 실제 동작 vs 확장 지점(스캐폴딩만) — ★ 정직하게 구분
**실제로 동작하는 운영 요소** (자신 있게 말해도 됨):
- Swagger/OpenAPI (JWT 보안스킴 포함 API 문서)
- Actuator + Prometheus (health/metrics 노출, K8s probe + 메트릭 수집)
- 표준 응답 `ApiResponse<T>` + 전역 예외 핸들러
- H2(개발)/PostgreSQL(운영) 프로파일 분리 — "URL만 바꿔 DB 교체 가능하게 JPA로 추상화"
- DataInitializer — 부팅 시 시드 데이터(admin 계정 + **실무 트러블슈팅 사례 11건을 개발노트로**)

**의존성/설정만 있고 실제로는 미작동** (절대 "구현했다"고 말하면 안 됨):
- **Sentry** — 의존성만, DSN 없음 → 미작동
- **Flyway** — `enabled: false` + 마이그레이션 스크립트 없음. 실제 스키마는 Hibernate `ddl-auto: update`가 관리
- **Cache** — `@EnableCaching`만 있고 `@Cacheable` 사용처 0건 → 캐시 안 걸림
- **BlogPost** — 엔티티만 있고 Controller/Service 없음 = **API 미구현**
- **테스트 코드 0개** — JUnit/Mockito 의존성은 있으나 `src/test`에 소스 없음

→ 소개 시: **"운영을 의식해 확장 지점을 미리 깔아뒀고(Cache/Flyway/Sentry/Blog), 실제 작동하는 건 인증·문서화·관측·프로파일 분리다"**로 말하면 과장이 없다. "왜 미리 깔았나"는 "확장 방향을 설계에 반영해뒀다"로 설명 가능.

### 3.4 정직하게 알아야 할 약점 (면접 질문 대비)
- JWT 필터가 유효 토큰에 **무조건 ROLE_ADMIN 부여** → 사실상 "로그인=관리자" 단일 모델. (Role enum은 있으나 인가에 미사용)
- JWT를 매 요청 3번 파싱 (비효율)
- `jwt.secret` 기본값이 소스에 평문 (운영은 env 주입 전제)
- CORS 이중 정의 (SecurityConfig + WebMvcConfigurer)
- 컨트롤러가 요청 바디를 `Map<String,String>`으로 받는 곳 다수 (빠른 구현 흔적)

---

## 4. 인프라 / DevOps — "이건 이거고, 왜 이렇게 했다" (진짜 셀링포인트)

로컬 Minikube 위에 프로덕션 K8s 패턴을 재현. **주니어 포트폴리오 치고 상위권으로 충실.**

### 4.1 안정성 3종 세트 (일관되게 구성한 게 강점)
| 무엇을 | 어떻게 | 왜 |
|---|---|---|
| 무중단 배포 | RollingUpdate `maxUnavailable:0, maxSurge:1` | "배포 중에도 서비스 0 없이" |
| PDB | backend/frontend `minAvailable:1` | "노드 드레인/축출 시 최소 1개 보장" |
| HPA | CPU 70% 기준 min2/max5 | "트래픽 증가 시 자동 수평 확장" |

### 4.2 보안 — NetworkPolicy 3계층 (주니어가 대개 빠뜨리는 부분)
트래픽을 `ingress → frontend → backend → postgres` 한 방향 체인으로만 강제:
- DB는 backend 파드만 접근 → 프론트가 탈취돼도 DB로 못 감(lateral movement 차단)
- backend는 frontend + ingress만
- 왜: "네트워크 심층방어(defense-in-depth) + 최소권한"

### 4.3 나머지 프로덕션 패턴
- **StatefulSet + RWO PVC + podAffinity** — DB는 상태 저장이라 StatefulSet, 업로드 PVC가 RWO라 파드를 같은 노드에 강제(RWO 제약 이해)
- **ResourceQuota + LimitRange** — 네임스페이스 자원 상한 + 파드 기본값 → 폭주 파드가 다른 워크로드 죽이는 것 방지
- **관측성** — actuator → micrometer → Prometheus → Grafana (코드부터 인프라까지 실제 연결됨)
- **CI/CD 2벌** — 레지스트리형(Docker Hub push) + 로컬형(minikube load). 로컬형은 **HTTP 응답 본문까지 검증하는 헬스게이트**
- **멀티스테이지 Docker 빌드** — 빌드 도구 제거로 이미지 경량화, 의존성 레이어 캐시로 빌드 가속
- **엣지 rate limiting + 보안 헤더** (Nginx: 로그인 분당 5회, API 20r/s)

### 4.4 인프라의 정직한 한계 (확장성 질문의 답)
- 단일 노드 Minikube + 단일 머신 → 진짜 멀티노드 확장 시 재설계 필요(RWO 스토리지→RWX/S3, DB→관리형/HA)
- DB replicas:1, 백업 없음 → 단일 장애점
- 외부 노출이 port-forward + DuckDNS + launchd → 취약, 실제 클라우드 아님
- **시크릿 평문 커밋** (secret.yaml의 DB비번/JWT) → 보안 리스크, 확장 전 정리 필요
- 로깅/분산추적 부재 (ELK/Loki/Jaeger 없음)

---

## 5. 확장성 관점 — "확장까지 생각하고 만들었나?"

**답: 부분적으로 그렇다. 설계엔 반영, 구현은 로컬 한계.**

**확장을 염두에 둔 흔적 (설명 가능)**:
- 수평 확장 경로: HPA + replicas + PDB
- 상태/무상태 분리: DB는 StatefulSet, 앱은 무상태(업로드만 PVC)
- 설정 외부화: ConfigMap/Secret로 환경별 재배포 없이 교체
- 백엔드 계층 분리 → "나중에 서비스 분리 가능"하게 설계(개발노트에 이 의도 문서화)
- 확장 지점 스캐폴딩: Cache(Redis 전환 여지), Flyway(마이그레이션 준비), Helm(클라우드 배포 밑그림)

**아직 확장 안 된 것 (정직하게)**:
- 실제 멀티노드/클라우드 아님, DB HA 없음, 로깅/추적 없음, 테스트 없음

→ **이 "확장 밑그림은 있으나 로컬에 갇힌" 상태가 바로 openclaw-msa/k-msa-platform으로 넘어가는 이유다.** "모놀리스를 MSA 플랫폼으로 확장하는 다음 단계"라는 서사로 자연스럽게 연결된다.

---

## 6. 소개 스크립트 (실제로 이렇게 말하라)

### 30초 버전
> "개발자 포트폴리오 사이트인데, 목적은 백엔드·DevOps 역량 증명입니다. React 프론트는 콘텐츠를 보여주는 렌더러이고, 콘텐츠는 전부 DB에 넣어 관리자 CMS로 관리합니다. 진짜 무게중심은 Spring Boot 백엔드(JWT 인증·레이트리미팅·관측성)와 로컬 Minikube 위에 프로덕션 K8s 패턴(무중단배포·PDB·HPA·NetworkPolicy·Prometheus·Jenkins CI/CD)을 재현한 인프라입니다."

### 2분 버전 (What → Why → 나의 의도)
> "먼저 **왜 이렇게 만들었냐면**, 저는 백엔드 개발자로서 '앱을 만들 줄 안다'를 넘어 '운영까지 안다'를 보여주고 싶었습니다.
> 그래서 **프론트는 의도적으로 얇게** 가고(원페이지 + 관리자 CMS), 콘텐츠를 DB로 빼서 코드 배포 없이 갱신되게 했습니다.
> **백엔드는** 계층을 명확히 분리하고, JWT·BCrypt·IP 레이트리미팅으로 보안 기본기를 챙기고, Swagger·Actuator로 문서화·관측을 붙였습니다. Cache·Flyway 같은 건 확장 지점으로 미리 깔아뒀고, 실제 작동하는 건 인증·문서화·관측·DB 프로파일 분리입니다.
> **인프라는** 로컬 Minikube지만 실제 프로덕션에서 신경 쓰는 것들 — 무중단 배포, PDB, HPA, NetworkPolicy 3계층 격리, ResourceQuota, Prometheus/Grafana, Jenkins 자동 배포 — 을 의도와 근거를 주석으로 남기며 재현했습니다.
> **한계도 압니다**: 단일 노드라 진짜 멀티노드 확장은 재설계가 필요하고, DB HA·로깅·테스트가 다음 과제입니다. **그래서 지금 이걸 MSA 플랫폼으로 확장하는 작업을 하고 있습니다.**"

### 면접에서 강한 한 방
> "확장을 염두에 두고 설계했지만, 저트래픽 개인 프로젝트에 무리하게 확장을 다 구현하는 건 오버엔지니어링이라 생각합니다. **언제 확장하고 언제 하지 말아야 하는지**를 판단하는 게 더 중요하다고 봅니다."

---

## 7. 소개 시 절대 틀리면 안 되는 사실 체크리스트

- ❌ "axios/framer-motion/react-hook-form을 썼다" → **미사용**(스캐폴드 잔재). fetch/CSS/useState로 직접 구현.
- ❌ "블로그 기능이 있다" → 엔티티만, **API 미구현**.
- ❌ "캐싱/Flyway/Sentry를 적용했다" → **선언만, 미작동**. "확장 지점으로 준비"가 정확.
- ❌ "테스트 커버리지" → **테스트 0개**.
- ❌ "역할 기반 권한(RBAC)" → 사실상 **단일 관리자 모델**(유효 토큰=ADMIN).
- ✅ 자신 있게: 계층형 아키텍처, JWT/BCrypt/레이트리미팅, Swagger/Actuator/Prometheus, K8s 안정성·보안·관측 패턴, Jenkins CI/CD, 멀티스테이지 Docker, DB 프로파일 분리, DevOps 트러블슈팅 사례.

---

> 참고: 이 문서는 실제 코드 분석 기반이며, "확장의 다음 단계"는 상위 `INTEGRATION_PLAN.md`(모놀리스→MSA 플랫폼 전환)로 이어진다.
