# services/analytics-service/ — 방문 기록·통계

> @PLAN  방문 이벤트 기록 + 통계 집계. 방문 기록 공개 / 통계 조회 보호.
> @FROM  K-portfolio: entity/Visitor.java, dto/VisitorStatsDTO.java, service/VisitorService.java, controller/VisitorController.java
> @HOW   Visitor 도메인 이관(Boot 3.2).
> @PHASE:5
> @LINK  ../../gateway/PLAN.md            (기록 공개 / 통계 보호 라우팅)
> @LINK  ../observability/PLAN.md         (앱 지표 vs 인프라 지표 구분 — 혼동 주의)

---

## 책임 범위
- 공개: `POST /visitors` (방문 기록)
- 보호: `GET /admin/visitors/stats` (통계)
- 스키마: `analytics`

<!--
@DEEP 채울 것:
  - Visitor 엔티티(ip/page/session/visited_at)
  - 통계 집계 쿼리(일별/페이지별)
  - @RISK 이건 "비즈니스 방문 통계"지 Prometheus 인프라 메트릭이 아님. observability와 역할 구분 명확히.
-->
