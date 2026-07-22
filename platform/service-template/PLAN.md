# platform/service-template/ — 새 서비스 찍어내는 골격 ★ (서비스 확장의 핵심)

> @PLAN  새 앱/서비스를 "복제 + 이름만 바꾸면" 되게 하는 표준 템플릿.
> @HOW   Boot 3.2 기준 최소 서비스 스켈레톤 + 플랫폼 연동이 기본 내장.
> @PHASE:0~
> @SCALE service
> @LINK  ../gateway/PLAN.md            (신규 서비스 자동 라우팅 등록 규약)
> @LINK  ../observability/PLAN.md      (계측이 템플릿에 기본 포함)
> @LINK  ../../docs/scalability.md#축-1-서비스-확장

---

## 왜 이게 확장성인가
서비스가 6개든 20개든, "새 서비스 추가 비용"이 일정하게 낮아야 진짜 확장 가능한 플랫폼이다.
템플릿이 없으면 서비스마다 보일러플레이트를 손으로 반복 → 확장 불가.

## 템플릿에 기본 포함되어야 할 것 (채울 자리)
- [ ] 표준 프로젝트 구조(controller/service/repository/entity/dto)
- [ ] Actuator + Micrometer 계측 (관측 기본 탑재) @LINK observability
- [ ] 트레이싱 헤더 전파 필터
- [ ] 표준 ApiResponse / GlobalExceptionHandler
- [ ] DB 스키마 초기화 규약
- [ ] Dockerfile + Helm values 조각
- [ ] 게이트웨이 등록 스니펫

<!--
@DEEP 실제 스켈레톤 코드 + "새 서비스 추가 5단계 체크리스트" 작성.
      이 파일이 "플랫폼 엔지니어" 신호의 핵심 — 흔한 포트폴리오엔 없음.
-->
