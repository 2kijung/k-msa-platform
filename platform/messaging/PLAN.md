# platform/messaging/ — 이벤트 브로커 (비동기 백본) ★ (통신 확장의 핵심)

> @PLAN  서비스 간 결합도를 낮추는 비동기 이벤트 백본. 동기 REST의 한계를 넘는다.
> @HOW   현재: 동기 REST + Resilience4j (ADR-0004). 다음 단계: Kafka 이벤트 전환.
> @PHASE:2 (첫 도입) ~ 7 (이벤트 전환 예정)
> @SCALE comm
> @LINK  ../../apps/portfolio/  (contact→notification 을 이벤트로)
> @LINK  ../observability/PLAN.md  (이벤트 흐름도 트레이싱 대상)
> @LINK  ../../docs/scalability.md#축-4-통신-확장

---

## 왜 이게 확장성인가

동기 REST는 호출자가 수신자를 직접 안다(강결합). 새 소비자 추가 = 발행자 코드 수정.
이벤트 발행/구독은 **발행자는 구독자를 모른다** → 새 서비스가 이벤트만 구독하면 확장 완료(기존 코드 무수정).

## 현재 구현 (동기 REST + 서킷브레이커)

```
contact-service --[RestTemplate POST]--> notification-service
                                         └── 서킷브레이커: 10건 중 50% 실패 시 OPEN
                                         └── Fallback: 로그만 남기고 본 흐름 계속
```

- 장점: 단순, Zipkin traceId 전파 명확
- 단점: notification 느려지면 contact 스레드 대기

## 목표 상태 (Phase 7 — 이벤트 전환)

```
contact-service --[publish]--> [Kafka: contact-events]
                                    --[subscribe]--> notification-service
                                    --[subscribe]--> analytics-service (코드 수정 0)
```

## 이벤트 카탈로그

| 이벤트 | 발행자 | 구독자 | 스키마 |
|---|---|---|---|
| ContactCreated | contact-service | notification-service | `{id, name, email, message, createdAt}` |
| ContactCreated | contact-service | analytics-service | `{id, pagePath, createdAt}` |
| BlogPublished | blog-service | notification-service | `{id, title, externalUrl}` |
| VisitorRecorded | analytics-service | (향후 추천 서비스) | `{ip, pagePath, visitedAt}` |

## 멱등성 · 재처리 · DLQ 설계

```java
// 구독자는 이벤트 ID로 중복 처리 방지
@KafkaListener(topics = "contact-events")
public void handleContactCreated(ContactCreatedEvent event) {
    if (processedEventRepo.existsById(event.getId())) return;  // 멱등성
    // 처리 로직
    processedEventRepo.save(new ProcessedEvent(event.getId()));
}
```

DLQ(Dead Letter Queue): 3회 재처리 실패 시 `contact-events-dlq`로 이동 → 운영자 알림.

## @RISK 최종적 일관성

동기 REST: contact 저장 → notification 호출 → 실패 시 notification 누락 (본 흐름은 정상)
이벤트: contact 저장 → 이벤트 발행 → notification 처리 실패 시 DLQ → 재처리

면접 대응: "최종적 일관성을 선택한 이유는 알림 누락이 문의 접수 자체보다 치명적이지 않기 때문.
비즈니스 로직(문의 저장)과 부가 기능(알림)을 분리해 핵심 흐름을 보호한다."
