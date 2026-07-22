# apps/ — 플랫폼 위의 독립 앱 카탈로그 ★

> @PLAN  플랫폼(platform/) 위에 얹히는 개별 앱들. 계속 추가 가능한 것이 확장성의 증거.
> @LINK  ../INTEGRATION_PLAN.md §2   (전체 구조)
> @LINK  ../platform/service-template/PLAN.md  (새 앱은 이 골격으로)
> @SCALE service

---

## 앱 목록

### app #1 — portfolio  (K-portfolio 분해)
> @FROM K-portfolio (Boot 3.2 모놀리식)  @PHASE:2~5
- `content-service/`   Project/Career/Skill/Certification/Profile  [기존 services/portfolio-content-service/ 재분류]
- `contact-service/`   문의 + 알림 연동                          [기존 services/contact-service/ 재분류]
- `analytics-service/` 방문 통계                                 [기존 services/analytics-service/ 재분류]

### app #2 — budget  (가계부) ★ 확장성 증거
> @FROM openclaw budget-service  @PHASE:5
> @PLAN 포트폴리오와 전혀 다른 도메인. "이 플랫폼은 이질 도메인도 수용한다"를 증명.
> @DEEP 가계부 메일파싱 배치·거래관리를 플랫폼(인증/관측/게이트웨이)에 편입

### app #3 — blog  (자동발행)
> @FROM openclaw blog-service + K-portfolio BlogPost/DevNote  @PHASE:4
> @PLAN Tistory/Velog 자동발행 + 예약 스케줄러. 앱 추가 확장성 첫 시연.
> @DEEP 엔티티 통합 + 자동발행 엔진 결합(스택 5-A/5-B 결정 대기)

---

## 새 앱 추가 규칙 (확장성 계약)
> @DEEP 채울 것: 새 앱은 (1) service-template 복제 (2) 플랫폼 인증/게이트웨이/관측 자동 연동
>       (3) 자기 DB 스키마 소유 (4) 다른 앱 DB 직접 접근 금지(분산 모놀리스 회피).
