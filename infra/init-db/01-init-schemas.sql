-- ============================================================================
-- infra/init-db/01-init-schemas.sql
-- ============================================================================
-- [무엇을] kmsa 데이터베이스 안에 "서비스별 스키마"를 미리 만든다.
-- [언제]   postgres 컨테이너가 처음 뜰 때 자동 1회 실행(docker-entrypoint-initdb.d 규약).
-- [왜/MSA] 하나의 PostgreSQL 인스턴스를 여러 서비스가 공유하되, 각 서비스는 자기 스키마만 본다.
--          → 서비스 간 데이터 격리(한 서비스가 다른 서비스 테이블을 직접 못 봄).
--          이게 'DB per service'의 리소스 현실적 절충(단일 인스턴스 + 스키마 분리)이다.
-- [왜 이렇게 미리 만드나] 각 서비스는 자기 스키마에 '테이블'만 자동 생성(ddl-auto)하지,
--          '스키마' 자체는 못 만드는 경우가 많다. 그래서 스키마는 인프라가 선제적으로 만든다.
-- ============================================================================

-- auth-service 전용 스키마 (지금 만드는 서비스)
CREATE SCHEMA IF NOT EXISTS auth;

-- ── 향후 서비스 확장 시 여기에 스키마를 추가한다 (지금은 주석으로 예약) ──
CREATE SCHEMA IF NOT EXISTS portfolio;     -- portfolio-content-service (Phase 3)
GRANT ALL ON SCHEMA portfolio TO kmsa;
CREATE SCHEMA IF NOT EXISTS contact;       -- contact-service (Phase 2)
GRANT ALL ON SCHEMA contact TO kmsa;
CREATE SCHEMA IF NOT EXISTS blog;          -- blog-service (Phase 4)
GRANT ALL ON SCHEMA blog TO kmsa;
CREATE SCHEMA IF NOT EXISTS analytics;     -- analytics-service (Visitor) Phase 5
GRANT ALL ON SCHEMA analytics TO kmsa;

-- [왜] kmsa 계정에 스키마 사용 권한 부여(초기 계정이 소유자라 보통 자동이나 명시).
GRANT ALL ON SCHEMA auth TO kmsa;
