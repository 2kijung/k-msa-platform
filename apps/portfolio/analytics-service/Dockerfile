# ============================================================================
# analytics-service / Dockerfile
# ============================================================================
# [무엇을] 방문자 통계 서비스. 포트 8087.
# [왜/MSA] Phase 5 — 읽기 부하 분리. analytics-service만 독립 스케일아웃.
# ============================================================================

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/analytics-service-1.0.0.jar app.jar

EXPOSE 8087

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8087/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
