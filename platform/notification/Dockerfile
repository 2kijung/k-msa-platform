# ============================================================================
# notification-service / Dockerfile (멀티스테이지 빌드)
# ============================================================================
# [무엇을] notification-service를 컨테이너 이미지로 빌드한다.
# [왜/MSA] 무상태(stateless) 알림 서비스. DB 없음. Telegram API만 호출.
#          contact-service가 REST로 호출 → 서비스 간 결합도 최소.
# ============================================================================

# ---- 1단계: 빌드 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# [왜] pom 먼저 → 의존성 레이어 캐시 (소스 수정 시 의존성 재다운로드 방지)
COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- 2단계: 실행 ----
FROM amazoncorretto:17-alpine3.21
WORKDIR /app

COPY --from=builder /build/target/notification-service-1.0.0.jar app.jar

# [왜/MSA] 8083 = notification-service 고정 포트. compose/게이트웨이가 이 포트로 접근.
EXPOSE 8083

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
