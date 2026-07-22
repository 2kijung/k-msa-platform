# platform/messaging/ — 이벤트 브로커 (비동기 백본) ★ (통신 확장의 핵심)

> @PLAN  서비스 간 결합도를 낮추는 비동기 이벤트 백본. 동기 REST의 한계를 넘는다.
> @HOW   Kafka 또는 RabbitMQ. 이벤트 발행/구독 계약 정의.
> @PHASE:2 (첫 도입) ~ 6 (확장)
> @SCALE comm
> @LINK  ../../apps/portfolio/  (contact→notification 을 이벤트로)
> @LINK  ../observability/PLAN.md  (이벤트 흐름도 트레이싱 대상)
> @LINK  ../../docs/scalability.md#축-4-통신-확장

---

## 왜 이게 확장성인가
동기 REST는 호출자가 수신자를 직접 안다(강결합). 새 소비자 추가 = 발행자 코드 수정.
이벤트 발행/구독은 **발행자는 구독자를 모른다** → 새 서비스가 이벤트만 구독하면 확장 완료(기존 코드 무수정).

## 첫 적용 시나리오 (채울 자리)
- `ContactCreated` 이벤트: contact-service 발행 → notification-service 구독(Telegram)
- 이후 확장 예: analytics도 같은 이벤트 구독해 "문의 유입 통계" — **발행자 수정 0**

<!--
@DEEP 채울 것:
  - 브로커 선택 근거(Kafka vs RabbitMQ) → ADR-0004
  - 이벤트 카탈로그(이름·스키마·발행자·구독자)
  - 멱등성·재처리·DLQ(실패 큐)
  - @RISK 최종적 일관성 — 면접 질문 2번(트랜잭션 정합성) 방어와 직결
-->
