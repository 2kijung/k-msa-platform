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

## 측정 결과

| 날짜 | 시나리오 | VU | TPS | p(95) | 에러율 | SLO | 비고 |
|---|---|---|---|---|---|---|---|
| **2026-08-02** | 로그인 — auth-service 직접 | 100 | **44.7 req/s** | **744ms** | **0.00%** | ❌ p95 초과 | auth-direct.js, 직접 8081 |
| **2026-08-02** | 로그인 — Nginx 게이트웨이 경유(:8090) | 100 | **42.9 req/s** | **1.29s** | **0.00%** | ❌ p95 초과 | auth-login.js, Nginx proxy 포함 |
| 미정 | 로그인 (K8s HPA 적용) | 100 | @DEEP | @DEEP | @DEEP | - | K8s HPA 연동 후 측정 |

### 2026-08-02 실측 상세 (auth-direct.js)

```
실측 환경:  Apple M4 Pro (ARM64), Docker Desktop, 로컬 단일 서버
시나리오:   워밍업 30s(20VU) → 100VU 1분 → 유지 1분 → 쿨다운 30s
대상:       auth-service:8081/auth/login 직접 (JWT 발급, BCrypt 검증 + PostgreSQL 조회)
총 요청:    8,081건 / 3분

TPS        44.7 req/s
avg        415.94ms
min        57.2ms
med        507.92ms
p(90)      701.4ms
p(95)      744ms   ← SLO(500ms) 초과
max        1,760ms
에러율      0.00%  ← 8,081건 전원 200 OK
check 통과  100%   (status 200 ✅, has token ✅)
```

### SLO 기준
- `p(95) < 500ms` — 실측 **744ms** (초과, 로컬 BCrypt+PostgreSQL 지연)
- `에러율 < 1%` — 실측 **0.00%** ✅

**SLO 초과 원인 분석:**
로컬 macOS Docker 환경에서 BCrypt 해싱(cost factor 10)이 CPU 부하의 주요 원인이다.
JVM 워밍업 + 단일 컨테이너 + PostgreSQL 쿼리가 복합된 결과로, 에러율 0%는 안정성 증명.
운영 K8s에서 HPA + 다중 레플리카 적용 시 TPS↑ p95↓ 예상 → @DEEP 재측정 필요.

### 측정 경로

```
k6 → auth-service:8081/auth/login → PostgreSQL:5432/auth.users (BCrypt 검증 + JWT 발급)
```

게이트웨이(Nginx:8090) 경유 재측 시: `k6 run load-test/auth-login.js`
(로컬 Caddy 포트 충돌으로 게이트웨이를 8090으로 변경, 기존 80은 k-devops.duckdns.org 담당)

<!--
@DEEP 다음 단계:
  - K8s HPA 적용 후 before/after 비교 측정
  - 병목이 나오면 원인 분석(커넥션풀/DB/스레드) → "근거 기반 최적화" 서사로 연결
  - Grafana 대시보드 스크린샷 첨부
-->
