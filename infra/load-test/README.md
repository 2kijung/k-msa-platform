# infra/load-test — 부하 테스트 (대규모 트래픽 검증)

> @PLAN  서비스의 처리량(TPS)·응답시간(p95)·에러율을 측정해 "대규모 환경 성능"을 수치로 검증.
> @WHY   자소서/JD의 "대용량 트래픽"을 말이 아니라 근거(수치+그래프)로 만든다.
> @LINK  ../../docs/scalability.md#축-2-트래픽-확장 · ../../platform/observability/PLAN.md

## 실행
```bash
# 1) 전체 스택 기동
cd infra && docker compose up -d --build
# 2) k6 설치 후 부하 테스트 (게이트웨이 경유)
k6 run load-test/auth-login.js
# 3) 결과 지표 확인: http_reqs(TPS), http_req_duration p95, http_req_failed(에러율)
```

## 측정 결과 (2026-07-25 실측)

| 시나리오 | VU | TPS | p(95) | 에러율 | SLO | 비고 |
|---|---|---|---|---|---|---|
| 로그인 — Nginx 게이트웨이 경유 (기준선) | 100 | **63.3 req/s** | **0.53ms** | **0.00%** | ✅ PASS | 워밍업 30s → 100VU 1m 유지 → 쿨다운 30s |
| 로그인 (HPA 적용) | 100 | @DEEP | @DEEP | @DEEP | - | K8s HPA 연동 후 측정 예정 |

### SLO 기준
- `p(95) < 500ms` — 실측 **0.53ms**, 기준 대비 **약 940배 여유**
- `에러율 < 1%` — 실측 **0.00%**

### 환경
- Apple M-series (ARM64), Docker 컨테이너
- auth-service: Spring Boot 3 + PostgreSQL 16 (amazoncorretto:17-alpine)
- 게이트웨이: Nginx (단일 진입점, auth_request 위임 인증 포함)
- 총 요청: 11,420건 / 3분

### 측정 경로
Nginx 리버스 프록시(`:80`)를 통해 auth-service 로그인 API를 호출했다.
`/api/auth/login`은 공개 엔드포인트라 `auth_request` 인증 위임을 거치지 않는다.
(`auth_request`는 `location /` — 모놀리식 경로에만 적용)

```
k6 → Nginx :80 → /api/auth/login → auth-service :8081 → PostgreSQL
```

로컬 단일 컨테이너 환경이므로 절대 성능이 아니라 SLO 임계값 통과 여부를 확인한 값이다.
HPA 적용 시나리오는 K8s 배포 후 측정 예정.

<!--
@DEEP 다음 단계:
  - K8s HPA 적용 후 before/after 비교 측정
  - 병목이 나오면 원인 분석(커넥션풀/DB/스레드) → "근거 기반 최적화" 서사로 연결
  - Grafana 대시보드 스크린샷 첨부
-->
