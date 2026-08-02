# ============================================================================
# blog-service / Dockerfile (멀티스테이지 빌드)
# ============================================================================
# [무엇을] 블로그 포스트 CRUD + Tistory·Velog 예약 자동발행. 포트 8086.
# [왜/MSA] Phase 4 — monolith에서 블로그 도메인 분리.
#          자동발행은 독립 배포 주기(content·contact와 분리)가 필요한 이유.
# ============================================================================

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/blog-service-1.0.0.jar app.jar

EXPOSE 8086

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
