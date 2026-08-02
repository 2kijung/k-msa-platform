# ============================================================================
# content-service / Dockerfile (멀티스테이지 빌드)
# ============================================================================
# [무엇을] content-service(Project·Career·Skill·Cert·Profile CRUD)를 컨테이너 이미지로.
# [왜/MSA] Phase 3 분리 서비스. portfolio 스키마 소유. 포트 8084.
#          auth 없음 — 게이트웨이가 인증 위임, X-User-Id 헤더만 신뢰.
# ============================================================================

# ---- 1단계: 빌드 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- 2단계: 실행 ----
FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/content-service-1.0.0.jar app.jar

# [왜/MSA] 8084 = content-service 고정 포트. 게이트웨이 라우팅 대상.
EXPOSE 8084

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
