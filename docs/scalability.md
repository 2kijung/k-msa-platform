# 확장성 설계 (Scalability & Extensibility) — 5축

> @PLAN  "개인 MSA 플랫폼"이 어떻게 커지는가. 서비스 6개에서 끝나지 않고 앱을 계속 얹는 골격.
> @LINK  ../INTEGRATION_PLAN.md §3   (5축 요약)

---

## 축 1. 서비스 확장 (Extensibility) — 새 앱을 어떻게 찍어내나 ★

> @SCALE service   @LINK ../platform/service-template/PLAN.md

**현재 구현:**
- `platform/service-template/` — 5단계 체크리스트 + 스켈레톤 코드 완성
- 새 서비스 = pom.xml 복사 + application.yml 포트·스키마 변경 + nginx.conf 2줄 추가

**서비스 추가 실측 소요 시간:**

| 단계 | 소요 |
|---|---|
| pom.xml + application.yml | 5분 |
| init-db SQL 스키마 추가 | 1분 |
| docker-compose 서비스 블록 | 3분 |
| nginx.conf upstream + route | 2분 |
| prometheus.yml job 추가 | 1분 |
| **합계** | **약 12분** |

→ Phase 3(content-service), Phase 4(blog-service), Phase 5(analytics-service) 모두 이 패턴으로 추가됨.

---

## 축 2. 트래픽 확장 (Scale-out) ★

> @SCALE traffic   @LINK ../infra/load-test/README.md

**k6 부하테스트 실측 (2026-08-02):**

| 시나리오 | VU | TPS | p95 | 에러율 |
|---|---|---|---|---|
| auth-service 직접 (:8081) | 100 | **44.7 req/s** | 744ms | 0.00% |
| Nginx 게이트웨이 경유 (:8090) | 100 | **42.9 req/s** | 1.29s | 0.00% |

게이트웨이 오버헤드: p95 기준 +549ms (auth_request 인증 검증 포함).

**K8s HPA 매니페스트 (`k8s/auth/hpa.yaml`):**

```yaml
spec:
  minReplicas: 2
  maxReplicas: 5
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          averageUtilization: 50   # CPU 50% 초과 시 자동 확장
```

**HPA 적용 명령:**

```bash
minikube addons enable metrics-server
kubectl apply -f k8s/auth/hpa.yaml
kubectl get hpa auth-service-hpa -n kmsa -w   # 실시간 모니터링
```

---

## 축 3. 데이터 확장

> @SCALE data   @LINK ../MIGRATION_DESIGN.md §4

**현재:** PostgreSQL 1 인스턴스 + 스키마 분리 (ADR-0002)

**다음 단계 설계:**
- **Redis 캐시** — content-service 프로젝트·스킬 목록 캐시 (@Cacheable)
- **읽기 복제** — analytics-service 통계 조회를 read replica로 분산
- **CQRS 기초** — AnalyticsService 쓰기·읽기 분리

---

## 축 4. 통신 확장 (결합도 ↓)

> @SCALE comm   @LINK ../platform/messaging/PLAN.md

**현재:** 동기 REST (contact → notification, ADR-0004)

**이벤트 전환 설계 (Phase 7 예정):**

```
[현재] contact-service --REST--> notification-service

[목표] contact-service --publish--> [ContactCreated 이벤트]
                                     --subscribe--> notification-service
                                     --subscribe--> analytics-service (코드 수정 0줄)
```

---

## 축 5. 환경·배포 확장

> @SCALE deploy   @LINK ../infra/

**현재 구현:**
- Docker Compose (로컬 개발, `infra/docker-compose.yml`)
- Kubernetes 매니페스트 (`k8s/`) — 6서비스 Deployment·Service·HPA
- Jenkinsfile — 6서비스 병렬 빌드·테스트·Docker Hub push·K8s Rolling Update

---

## ⚖️ 확장성의 함정

확장성을 말로만 하면 감점. 각 축에 최소 1개 "동작하는 증거" 남김:

| 축 | 증거 |
|---|---|
| 서비스 확장 | service-template 5단계 체크리스트 + Phase 3~5에서 3개 서비스 실제 추가 |
| 트래픽 확장 | k6 TPS 44.7 실측 + HPA 매니페스트 (`k8s/auth/hpa.yaml`) |
| 데이터 확장 | 스키마 분리 코드 (currentSchema=analytics) + 기간별 통계 API |
| 통신 확장 | Resilience4j 서킷브레이커 + Zipkin 3 spans traceId 전파 |
| 배포 확장 | Jenkinsfile 병렬 빌드 + K8s 매니페스트 일체 |
