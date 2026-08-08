# ADR-0003: API 게이트웨이로 Spring Cloud Gateway 대신 Nginx 채택

## 상태

**채택됨 (Adopted)** — Phase 1부터

## 맥락

API 게이트웨이 옵션:
1. Spring Cloud Gateway (SCG) — Java 기반, Spring 생태계 통합, 코드로 라우팅
2. Nginx — 범용 프록시, 설정 파일 기반, auth_request 내장
3. Kong — 플러그인 기반, 운영 부담 큼

## 결정

**Nginx** 채택.

이유:
1. `auth_request` 지시어 — 외부 auth-service에 인증 위임 1줄로 처리. SCG는 커스텀 필터 작성 필요.
2. 포트폴리오 맥락에서 Nginx는 실무에서 이미 사용 경험 → 빠른 구현
3. 설정 파일(nginx.conf)이 라우팅 규칙 문서 역할 → 가독성

## 결과

**장점:**
- auth_request로 게이트웨이 레벨 인증 위임 구현 → 각 서비스 인증 로직 0줄
- X-User-Id 헤더 주입으로 downstream 서비스에 사용자 정보 전파
- upstream 블록으로 서비스 추가 시 2줄(upstream + location) 추가

**비용:**
- Lua 없이는 복잡한 라우팅 로직 구현 어려움
- SCG 대비 서킷브레이커·재시도 내장 없음 → Resilience4j를 서비스 레벨에서 별도 적용

**향후:** 서비스가 10개 이상이거나 동적 라우팅이 필요해지면 Spring Cloud Gateway로 교체 (ADR 재작성 예정).
