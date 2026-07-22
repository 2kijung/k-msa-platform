# gateway/ — API 게이트웨이 (전체 시스템의 단일 진입점)

> @PLAN  모든 외부 요청의 단일 관문. 인증 검증 + X-User-Id 주입 + 서비스 라우팅.
> @FROM  openclaw-msa: k8s/nginx-gateway.conf (auth_request 패턴 이미 구현됨)
> @HOW   기존 nginx.conf를 재사용/확장. Phase 6에서 Spring Cloud Gateway 승격 검토(어필↑).
> @PHASE:0
> @LINK  services/auth-service/PLAN.md   (검증 요청을 여기로 보냄)
> @LINK  ../MIGRATION_DESIGN.md §6        (라우팅 확장 상세)

---

## 왜 게이트웨이가 핵심인가 (전시 관점)
"MSA 흉내(프로세스만 여러 개)"와 "진짜 MSA"를 가르는 첫 관문. 프론트는 서비스가 몇 개로 쪼개졌는지 몰라야 한다(`/api/*` 하나로 은닉).

## 라우팅 지도 (채울 자리)
| 경로 | → 서비스 | 인증 | 상태 |
|---|---|---|---|
| `/api/auth/login` | auth-service | 공개 | @DEEP |
| `= /auth/verify` (internal) | auth-service | — | @DEEP |
| `/api/projects` (GET) | portfolio-content | 공개 | @DEEP |
| `/api/contacts` (POST) | contact-service | 공개 | @DEEP |
| `/api/visitors` (POST) | analytics-service | 공개 | @DEEP |
| `/api/admin/**` | (다중) | 보호(auth_request) | @DEEP |

<!--
@DEEP 여기에 채울 것:
  - upstream 블록 (서비스별 host:port)
  - auth_request → auth-service /auth/verify 연결, auth_request_set X-User-Id
  - 공개/보호 라우트 분리 규칙
  - 내부 전용(/api/notify/) CIDR 화이트리스트
  - Phase 6: Spring Cloud Gateway 전환 시 필터 체인(트레이싱 헤더 전파 @LINK observability)
-->
