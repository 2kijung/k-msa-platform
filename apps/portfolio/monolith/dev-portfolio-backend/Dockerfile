# Multi-stage build
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage (amazoncorretto:17-alpine3.21 supports ARM64/amd64)
FROM amazoncorretto:17-alpine3.21

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/portfolio-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Health check
# [수정] monolith 공개 API 사용 (auth_request 불필요, Spring Security permitAll 경로)
# /api/auth/health는 auth 제거 후 403 반환, /actuator는 SPA index.html 반환
# → /api/projects로 변경 (GET, permitAll)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/api/projects || exit 1

# Run application (prod profile activates PostgreSQL datasource)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
