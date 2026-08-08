# ADR-0002: DB 분리 전략 — DB per Service 대신 Schema per Service 채택

## 상태

**채택됨 (Adopted)** — Phase 0부터 전 서비스 적용

## 맥락

MSA 원칙은 서비스마다 독립 DB를 갖는 것(DB per Service)이다.
하지만 로컬 개발·포트폴리오 환경에서 PostgreSQL 인스턴스 6개를 별도로 운영하면:
- 포트 6개 추가 필요 (리소스)
- Docker Compose 복잡도 증가
- dev-prod parity 유지 어려움

## 결정

PostgreSQL 인스턴스 1개 + 서비스별 스키마 분리.

```sql
CREATE SCHEMA auth;      -- auth-service 전용
CREATE SCHEMA portfolio; -- content-service 전용
CREATE SCHEMA contact;   -- contact-service 전용
CREATE SCHEMA blog;      -- blog-service 전용
CREATE SCHEMA analytics; -- analytics-service 전용
```

각 서비스는 `currentSchema=<schema>` + `hibernate.default_schema`로 자기 스키마만 접근.

## 결과

**장점:**
- PostgreSQL 1개로 6개 서비스 분리 → 리소스·복잡도 절감
- JPA `default_schema` 설정으로 코드 레벨에서 타 스키마 접근 불가 → 데이터 격리 강제
- "DB per service 원칙을 현실적으로 절충했고 그 이유를 안다" → 면접 포인트

**비용:**
- DB 장애 시 전 서비스 영향 → 단일 장애점 (DB per service 대비 약점)
- 스키마 간 JOIN 기술적으로 가능 → 팀 규약 필요 (코드 리뷰에서 차단)

**교훈:**
- "원칙과 현실의 절충을 설명할 수 있다"가 포트폴리오 가치. 원칙을 모르는 게 아니라 의도적 절충.
- 운영 전환 시 서비스별 DB로 분리하는 마이그레이션 전략 필요 (ADR-0005 예정)
