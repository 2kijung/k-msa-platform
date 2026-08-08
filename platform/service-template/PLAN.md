# platform/service-template/ — 새 서비스 찍어내는 골격 ★ (서비스 확장의 핵심)

> @PLAN  새 앱/서비스를 "복제 + 이름만 바꾸면" 되게 하는 표준 템플릿.
> @HOW   Boot 3.2 기준 최소 서비스 스켈레톤 + 플랫폼 연동이 기본 내장.
> @PHASE:0~
> @SCALE service
> @LINK  ../gateway/PLAN.md            (신규 서비스 자동 라우팅 등록 규약)
> @LINK  ../observability/PLAN.md      (계측이 템플릿에 기본 포함)
> @LINK  ../../docs/scalability.md#축-1-서비스-확장

---

## 왜 이게 확장성인가

서비스가 6개든 20개든, "새 서비스 추가 비용"이 일정하게 낮아야 진짜 확장 가능한 플랫폼이다.
템플릿이 없으면 서비스마다 보일러플레이트를 손으로 반복 → 확장 불가.

## ✅ 새 서비스 추가 5단계 체크리스트

### Step 1. pom.xml 복제 + 서비스명 변경

```xml
<artifactId>my-service</artifactId>
<description>내 새 서비스 설명</description>
<!-- 아래 의존성은 모든 서비스 공통 — 건드리지 말 것 -->
<!-- spring-boot-starter-web, data-jpa, actuator, prometheus, tracing-bridge-brave, zipkin-reporter-brave -->
```

### Step 2. application.yml 포트·스키마 설정

```yaml
server:
  port: 8088          # 기존 서비스와 겹치지 않는 포트

spring:
  application:
    name: my-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:kmsa}?currentSchema=myschema
  jpa:
    properties:
      hibernate:
        default_schema: myschema   # 이 스키마만 본다 (데이터 격리)

management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://${ZIPKIN_HOST:zipkin}:9411/api/v2/spans
```

### Step 3. infra/init-db/01-init-schemas.sql에 스키마 추가

```sql
CREATE SCHEMA IF NOT EXISTS myschema;
GRANT ALL ON SCHEMA myschema TO kmsa;
```

### Step 4. infra/docker-compose.yml에 서비스 추가

```yaml
my-service:
  build:
    context: ../apps/my-domain/my-service
  container_name: kmsa-my
  depends_on:
    postgres:
      condition: service_healthy
  environment:
    DB_HOST: postgres
    DB_USER: kmsa
    DB_PASSWORD: kmsa1234
    ZIPKIN_HOST: zipkin
  ports:
    - "8088:8088"
```

### Step 5. platform/gateway/nginx.conf에 upstream + route 추가

```nginx
upstream myservice { server my-service:8088; }

# 공개 조회
location /api/my-domain/ { proxy_pass http://myservice/my/; }
```

### Step 6. platform/observability/prometheus.yml에 job 추가

```yaml
- job_name: 'my-service'
  static_configs:
    - targets: ['my-service:8088']
  metrics_path: /actuator/prometheus
```

---

## 템플릿에 기본 포함된 것 (스켈레톤 확인)

| 항목 | 파일 | 상태 |
|---|---|---|
| 표준 프로젝트 구조 | src/main/java/.../controller/service/repository/entity/ | ✅ |
| Actuator + Micrometer 계측 | pom.xml + application.yml | ✅ |
| Zipkin 트레이싱 | micrometer-tracing-bridge-brave + application.yml | ✅ |
| 표준 ApiResponse | common 패턴 참조 | ✅ |
| DB 스키마 격리 | currentSchema + default_schema | ✅ |
| Dockerfile (멀티스테이지) | Dockerfile | ✅ |
| K8s 매니페스트 | k8s/{service}/deployment.yaml | ✅ |
| 게이트웨이 upstream | nginx.conf 추가 패턴 | ✅ |
| Prometheus 등록 | prometheus.yml 추가 패턴 | ✅ |

## 실제 스켈레톤 소스 위치

`platform/service-template/src/` — Spring Boot 3.2 최소 서비스 골격.
이 폴더를 통째로 복사 → groupId/artifactId/패키지명만 교체하면 빌드 가능.
