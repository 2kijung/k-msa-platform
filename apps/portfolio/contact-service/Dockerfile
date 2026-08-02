# ============================================================================
# contact-service / Dockerfile (멀티스테이지 빌드)
# ============================================================================
# [무엇을] contact-service를 컨테이너 이미지로 빌드한다.
# [왜/MSA] 문의 접수·조회 전담 서비스. PostgreSQL contact 스키마 소유.
#          접수 후 notification-service REST 호출 → 알림 발송(실패 시 본흐름 무중단).
# ============================================================================

# ---- 1단계: 빌드 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# [왜] pom 먼저 → 의존성 레이어 캐시
COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- 2단계: 실행 ----
FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/contact-service-1.0.0.jar app.jar

# [왜/MSA] 8085 = contact-service 고정 포트.
EXPOSE 8085

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
