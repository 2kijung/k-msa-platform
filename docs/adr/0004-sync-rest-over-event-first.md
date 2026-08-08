# ADR-0004: 서비스 간 통신 — 이벤트 브로커 대신 동기 REST 우선 채택

## 상태

**채택됨 (Adopted)** — Phase 2. 이벤트 전환은 Phase 7 예정.

## 맥락

서비스 간 통신 방식:
1. 동기 REST — 즉각 응답, 구현 단순, 결합도 높음
2. 비동기 이벤트 (Kafka/RabbitMQ) — 느슨한 결합, 복잡도 높음, 재처리·멱등성 설계 필요

contact-service → notification-service 통신을 어떻게 할 것인가.

## 결정

**동기 REST 먼저, 이벤트 나중.**

이유:
1. 포트폴리오 1단계 목표: "서비스 분리 자체를 증명"하는 것. 이벤트는 그 다음 단계.
2. Kafka 운영 복잡도(브로커 클러스터, 토픽 관리, 컨슈머 그룹) → 핵심 가치 흐림
3. REST는 Zipkin traceId로 동기 추적 가능 → 관측성 증명에 더 명확

## 결과

**장점:**
- contact → notification REST 호출: RestTemplate + Resilience4j 서킷브레이커 조합
- Zipkin에서 contact→notification 3 spans 단일 trace로 연결 확인
- 구현 기간 단축 → 관측성(Zipkin·Prometheus·Grafana) 완성에 더 집중 가능

**비용:**
- notification 느려지면 contact 스레드 대기 → Resilience4j timeout 2s로 부분 보완
- 새 서비스가 알림 받으려면 notification 코드 수정 필요 (이벤트 방식은 구독만 추가)

**교훈:**
- Resilience4j @CircuitBreaker (CLOSED→OPEN→HALF-OPEN 전환) 직접 구현으로 회복탄력성 패턴 체득
- "왜 이벤트를 안 썼는가"를 설명할 수 있으면 오히려 "이벤트도 안다"는 신호
