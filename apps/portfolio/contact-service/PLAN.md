# services/contact-service/ — 문의 접수 + 알림 연동

> @PLAN  방문자 문의 접수·조회. 문의 도착 시 notification-service로 알림.
> @FROM  K-portfolio: entity/Contact.java, service/ContactService.java, controller/ContactController.java
> @HOW   Contact 도메인 이관(Boot 3.2). 알림은 notification-service를 REST 호출(초기) → 이벤트 기반 승격(Phase 6).
> @PHASE:2
> @LINK  ../../notification-service/       (기존 Telegram 서비스 재사용)
> @LINK  ../../MIGRATION_DESIGN.md §3.2    (서비스 간 REST 계약)
> @LINK  ../observability/PLAN.md          (동기→이벤트 전환이 면접 방어 포인트)
> @RISK  알림 실패가 문의 접수를 막으면 안 됨 → openclaw NotificationClient의 "예외 삼킴" 패턴 계승.

---

## 책임 범위
- 공개: `POST /contacts` (문의 제출)
- 보호: `GET /admin/contacts` (관리자 조회)
- 스키마: `contact`

<!--
@DEEP 채울 것:
  - Contact 엔티티/상태(NEW/READ/REPLIED)
  - notification 호출 클라이언트 (openclaw NotificationClient 패턴)
  - ★ Phase 6: REST 동기 호출 → 메시지 브로커(Kafka/RabbitMQ) 이벤트 발행으로 전환
       "ContactCreated" 이벤트 → notification이 구독. (이벤트 기반 아키텍처 시연)
-->
