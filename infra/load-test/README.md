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

## 측정할 것 (채울 자리 — 실행 후 기록)
| 시나리오 | VU | TPS | p95(ms) | 에러율 | 비고 |
|---|---|---|---|---|---|
| 로그인 (HPA 미적용) | 100 | @DEEP | @DEEP | @DEEP | 기준선 |
| 로그인 (HPA 적용) | 100 | @DEEP | @DEEP | @DEEP | 오토스케일 효과 |

<!--
@DEEP 실제 측정 후:
  - 위 표를 채우고, before/after 그래프 스크린샷을 docs 또는 README 상단에 첨부
  - 이 수치가 자소서 2번/5번의 "부하 테스트로 검증" 근거가 된다
  - 병목이 나오면 원인 분석(커넥션풀/DB/스레드) → "근거 기반 최적화" 서사로 연결
-->
