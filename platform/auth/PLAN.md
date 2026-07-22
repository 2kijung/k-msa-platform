# services/auth-service/ — 인증 중앙화 (MSA 단일 로그인)

> @PLAN  로그인·JWT 발급·검증·사용자 관리. MSA 인증 흐름의 시작점.
> @FROM  K-portfolio: entity/User.java, service/AuthService.java, security/JwtUtil.java, security/JwtAuthenticationFilter.java, config/SecurityConfig.java
> @FROM  openclaw-msa: budget-service의 /auth/login·/auth/verify 흐름, openclaw-common/JwtUtil 개념
> @HOW   K-portfolio(Boot 3.2) 인증 코드를 이관 → 독립 서비스로. javax/jakarta 정리는 Boot 기준으로 통일.
> @PHASE:1
> @LINK  ../../gateway/PLAN.md            (게이트웨이가 /auth/verify로 검증 위임)
> @LINK  ../../MIGRATION_DESIGN.md §3.1   (인증 전파 시퀀스)
> @RISK  하위 서비스는 JWT를 다시 파싱하지 말 것 → X-User-Id 헤더만 신뢰(내부망 전제). 분산 모놀리스 회피.

---

## 책임 범위
- `POST /auth/login` → 자격 검증 → JWT 발급
- `GET /auth/verify` → Nginx auth_request 전용, 검증 후 `X-User-Id`/`X-User-Role` 헤더 반환
- 사용자 스키마: `auth` (users 테이블)

<!--
@DEEP 채울 것:
  - User 엔티티/Repository (auth 스키마)
  - JWT 발급·검증(만료·리프레시·서명키 외부화 Secret)
  - verify 응답 헤더 계약(X-User-Id, X-User-Role) — gateway와 합의
  - BCrypt 인코더, 초기 admin seed (K-portfolio DataInitializer 참고)
  - Phase 6: 트레이싱 컨텍스트 전파, 로그인 실패율 메트릭
-->
