# services/portfolio-content-service/ — 포트폴리오 콘텐츠 CRUD

> @PLAN  Project/Career/Skill/Certification/Profile 콘텐츠 관리. 조회 공개 / 관리 보호.
> @FROM  K-portfolio: entity·repository·service·controller 의 Project/Career/Skill/Certification/Profile 일체
> @HOW   K-portfolio 도메인 모듈을 거의 그대로 이관(Boot 3.2). 인증 로직은 제거(게이트웨이+auth-service가 담당).
> @PHASE:3
> @LINK  ../auth-service/PLAN.md          (관리 API는 X-User-Id 헤더 신뢰)
> @LINK  ../../gateway/PLAN.md            (GET 공개 / admin 보호 라우팅)

---

## 책임 범위
- 공개: `GET /projects`, `GET /careers`, `GET /skills` ...
- 보호: `POST/PUT/DELETE /admin/**` (게이트웨이에서 검증 후 헤더 주입)
- 스키마: `portfolio`

<!--
@DEEP 채울 것:
  - 엔티티 5종 이관 + 매핑 확인
  - 파일 업로드(FileController) 분리 여부 결정 → file-service? (MIGRATION_DESIGN §11-3)
  - display_order/featured 정렬 규칙
  - 캐싱(K-portfolio CacheConfig 재사용 여부)
-->
