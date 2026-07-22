# k-msa-platform

> **모놀리스 → MSA 전환**을 실제로 수행하며, 여러 앱을 얹는 **개인 MSA 플랫폼**을 구축하는 프로젝트.
> 이 저장소의 **커밋 히스토리 자체가 "전환 과정"의 기록**이다.

---

## 이게 뭔가

포트폴리오 사이트 하나가 아니라, **여러 독립 앱을 운영하는 플랫폼**이다.
모놀리식으로 만든 서비스(K-portfolio)를 출발점으로, 게이트웨이 뒤에서 도메인을 하나씩
서비스로 떼어내며(Strangler Fig) MSA로 전환한다. 그 위에 가계부·블로그 등 이질 도메인 앱을 얹어
**플랫폼의 확장성**을 증명한다.

## 왜 (전환 서사)

- **출발점**: 모놀리식 실무 경험. "왜 쪼개야 하는가"를 몸으로 안다.
- **이 프로젝트**: 그 모놀리스를 직접 MSA로 전환 — 서비스 경계, 인증 중앙화, 관측성, 회복탄력성.
- **핵심 메시지**: "왜 쪼개는지 · **언제 쪼개면 안 되는지** · 어떻게 확장 가능하게 만드는지"를 판단할 수 있다.

> ⚠️ 이 워크로드에는 모듈러 모놀리스가 더 실용적이다. MSA는 **역량 시연을 위한 의도적 선택**이며,
> 실무에선 Monolith-First 원칙을 따른다. (상세: `MIGRATION_DESIGN.md §12`)

## 구조

```
platform/   앱들이 공유하는 공통 계층 (확장의 척추)
├── gateway/         단일 진입점·라우팅·인증위임
├── auth/            중앙 인증 (JWT)
├── observability/   관측·트레이싱·회복탄력성
├── messaging/       이벤트 브로커 (비동기 백본)
└── service-template/ 새 서비스 찍어내는 골격
apps/       플랫폼 위의 독립 앱 (계속 추가 가능)
├── portfolio/       앱 #1 (K-portfolio 분해: content·contact·analytics)
├── budget/          앱 #2 (가계부 — 이질 도메인 = 확장성 증거)
└── blog/            앱 #3 (블로그 자동발행)
infra/      환경·배포 확장 (helm·argocd·terraform·멀티환경)
docs/       설계·결정·서사 (scalability·adr·interview-narrative)
```

## 문서 지도

| 문서 | 내용 |
|---|---|
| `INTEGRATION_PLAN.md` | 통합 마스터 지도 + 주석 마커 컨벤션 + 확장성 5축 |
| `MIGRATION_DESIGN.md` | 상세 마이그레이션 설계 + 전시 전략 |
| `docs/scalability.md` | 확장성 5축 (서비스/트래픽/데이터/통신/배포) |
| `docs/adr/` | 아키텍처 결정 기록 |
| `docs/interview-narrative.md` | 면접 5대 질문 방어 + 전환 서사 |

## 진행 방식

**스캐폴딩 우선 → 점진 심화.** 각 위치에 `@PLAN/@FROM/@HOW/@DEEP/@LINK/@SCALE` 주석 마커를 심고,
`@DEEP` 지점을 따라가며 부분부분 딥하게 채우고 서로 연결한다.

```bash
grep -rn "@DEEP" .   # 아직 채우지 않은 지점 추적
```

## 로드맵 (Strangler Fig)

| Phase | 내용 |
|---|---|
| 0 | 플랫폼 레이아웃 확정, K-portfolio 모놀리스 흡수(As-Is 기준점), 게이트웨이 통째 라우팅 |
| 1 | platform/auth 분리 (인증 중앙화) |
| 2 | apps/portfolio/contact + 알림 (messaging 백본 첫 도입) |
| 3 | apps/portfolio/content 분리 |
| 4 | apps/blog 통합 (엔티티 + 자동발행) |
| 5 | apps/portfolio/analytics + apps/budget 편입 (확장성 증거) |
| 6 | 관측성·트레이싱·회복탄력성 + infra(멀티환경/GitOps) + 전환 스토리 |

---

<!--
@DEEP 이 README에 추후 추가:
  - 아키텍처 다이어그램
  - Grafana/Zipkin 스크린샷 (관측성 완성 후)
  - 각 앱 실행 방법 (docker-compose)
  - 계획 MD 내부 @LINK 경로를 새 구조(platform/apps)에 맞게 정합화
-->
