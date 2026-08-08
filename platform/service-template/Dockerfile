# ============================================================================
# service-template / Dockerfile — 새 서비스 표준 빌드
# ============================================================================
# [사용법] 복사 후 JAR 파일명(my-service-1.0.0.jar)만 변경

FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /build

# [왜 pom.xml 먼저] 소스 변경 시 의존성 재다운로드 방지 (Docker layer cache 활용)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# [왜 amazoncorretto] arm64/amd64 멀티아키 지원 (M1 Mac + CI 서버 모두 동작)
FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/my-service-1.0.0.jar app.jar   # ★ JAR명 변경

EXPOSE 8088   # ★ 포트 변경

# [왜] 서비스별 실제 동작 확인 엔드포인트. 루트(/)는 404 가능.
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8088/my-domain/health || exit 1   # ★ 경로 변경

ENTRYPOINT ["java", "-jar", "app.jar"]
