# ============================================================================
# auth-service / Dockerfile (멀티스테이지 빌드)
# ============================================================================
# [무엇을] auth-service를 "어디서든 똑같이 도는 컨테이너 이미지"로 굽는다.
# [왜/MSA] MSA는 서비스마다 자기 이미지를 갖고 독립 배포한다. 이 Dockerfile이 그 단위다.
# [왜 멀티스테이지] 빌드 도구(Maven/JDK 풀)를 최종 이미지에서 제거 → 이미지 경량 + 공격 표면 축소.
#          또 로컬에 Maven이 없어도 docker가 빌드 단계에서 알아서 빌드한다(빌드 환경 일치).
# ============================================================================

# ---- 1단계: 빌드 (Maven + JDK17) ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# [왜] pom만 먼저 복사해 의존성을 받아둔다. pom이 안 바뀌면 이 레이어가 캐시되어
#      소스만 고쳤을 때 의존성을 다시 안 받는다(빌드 가속).
COPY pom.xml .
RUN mvn -q -e dependency:go-offline

# [왜] 이제 소스를 복사해 실제 빌드. 테스트는 이미지 빌드에서 스킵(CI에서 별도 수행 전제).
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- 2단계: 실행 (JRE만) ----
# [왜] jre-alpine = 실행에 필요한 최소 런타임. JDK/Maven 없음 → 작고 안전.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# [무엇을] 1단계에서 만든 실행 가능 jar만 가져온다. (artifactId-version = auth-service-1.0.0.jar)
COPY --from=builder /build/target/auth-service-1.0.0.jar app.jar

# [왜/MSA] 이 서비스의 포트. 게이트웨이/compose가 이 포트로 접근.
EXPOSE 8081

# [왜/MSA] 컨테이너 자체 헬스체크. 오케스트레이터(compose/K8s)가 이걸로 생사 판단 → Self-Healing 기반.
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -q --spider http://localhost:8081/auth/health || exit 1

# [무엇을] 컨테이너 시작 시 실행할 명령. DB 접속 정보/프로필은 환경변수로 주입받는다(compose/K8s).
ENTRYPOINT ["java", "-jar", "app.jar"]
