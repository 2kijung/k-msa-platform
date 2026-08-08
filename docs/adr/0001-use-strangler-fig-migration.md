# ADR-0001: 모놀리스 → MSA 전환에 Strangler Fig 패턴 채택

> ADR(Architecture Decision Record) = "왜 이렇게 결정했는지" 기록. 면접에서 그대로 근거로 쓴다.
> @PHASE: 전 과정 관통
> @LINK  ../../MIGRATION_DESIGN.md §2

## 상태

**채택됨 (Adopted)** — Phase 0~6 완료 (2026-08-08)

## 맥락

K-portfolio(Spring Boot 3.2 모놀리식)를 한 번에 재작성하면:
- 전환 중 기존 서비스 중단 위험
- 실패 시 롤백 불가
- "전환을 직접 설계했다"는 서사가 사라짐

## 결정

게이트웨이 뒤에서 도메인을 하나씩 서비스로 떼어낸다(Strangler Fig).
각 단계는 Nginx 라우트 전환만으로 롤백 가능.

```
모놀리스 → [Phase 1] auth 분리
         → [Phase 2] contact·notification 분리
         → [Phase 3] content 분리
         → [Phase 4] blog 통합
         → [Phase 5] analytics 분리
         → [Phase 6] 관측성 완성
```

## 결과 (실측, 2026-08-08)

**장점:**
- 각 Phase 완료 후 즉시 검증 가능 → Phase 3 완료 후 content-service healthy 확인
- 게이트웨이 라우트만 바꾸면 롤백 — 실제로 gateway 포트 80→8090 변경 시 1줄 수정
- 모놀리스가 살아있으므로 서비스 미분리 구간도 정상 운영 (5일 uptime 유지)

**비용:**
- 전환 기간 동안 모놀리스·신규 서비스 공존 → Docker Compose 컨테이너 12개
- Nginx auth_request 라우팅 복잡도 증가 → nginx.conf 관리 필요
- 게이트웨이 upstream 설정 실수로 502 발생 1회 (재시작으로 해결)

**교훈:**
- `contact→notification` 동기 REST 호출에서 RestTemplate 초기화 방식이 Zipkin traceId 전파에 영향 → new RestTemplate() ❌ / RestTemplateBuilder.build() ✅
- Docker Compose project name 충돌 (k-msa-platform/infra/ ↔ openclaw-msa/infra/) → `name: openclaw-msa` 명시로 해결
- BusyBox wget은 GNU wget 플래그(`--spider`, `--no-verbose`) 미지원 → `-q -O /dev/null` 사용
