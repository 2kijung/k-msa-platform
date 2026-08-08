# k8s/ — Kubernetes 배포 매니페스트

## 구조

```
k8s/
├── auth/           auth-service: Deployment·Service·HPA
├── content/        content-service: Deployment·Service
├── contact/        contact-service: Deployment·Service
├── blog/           blog-service: Deployment·Service
├── analytics/      analytics-service: Deployment·Service
├── notification/   notification-service: Deployment·Service
└── monitoring/     Prometheus·Grafana·Zipkin K8s 배포
```

## 전체 배포 순서

```bash
# 1. 네임스페이스 생성
kubectl create namespace kmsa

# 2. Secrets 생성 (실제 값으로 교체)
kubectl create secret generic kmsa-secrets \
  --from-literal=jwt-secret=<YOUR_JWT_SECRET_32BYTES> \
  --from-literal=db-user=kmsa \
  --from-literal=db-password=<YOUR_DB_PASSWORD> \
  -n kmsa

# 3. metrics-server 활성화 (HPA 전제 조건)
minikube addons enable metrics-server

# 4. 서비스 배포
kubectl apply -f k8s/auth/
kubectl apply -f k8s/content/
kubectl apply -f k8s/contact/
kubectl apply -f k8s/notification/
kubectl apply -f k8s/blog/
kubectl apply -f k8s/analytics/

# 5. HPA 상태 확인
kubectl get hpa -n kmsa
```

## HPA 부하 테스트 시나리오

```bash
# 1. 부하 전 Pod 수 확인
kubectl get pods -n kmsa -l app=auth-service

# 2. k6 부하 시작 (별도 터미널)
k6 run --env BASE_URL=http://$(minikube ip):8081 infra/load-test/auth-direct.js

# 3. 실시간 HPA 모니터링
kubectl get hpa auth-service-hpa -n kmsa -w

# 4. 부하 후 Pod 수 확인 (자동 확장 확인)
kubectl get pods -n kmsa -l app=auth-service
```

## [왜/MSA] Docker Compose vs Kubernetes

| 기능 | Docker Compose | Kubernetes |
|---|---|---|
| 기동 방식 | docker compose up | kubectl apply |
| Self-healing | ❌ (컨테이너 재시작만) | ✅ (Pod 자동 재생성) |
| Auto-scaling | ❌ | ✅ (HPA) |
| Rolling update | ❌ | ✅ (무중단) |
| Secret 관리 | 환경변수 파일 | K8s Secret (암호화) |
| 서비스 디스커버리 | DNS (컨테이너명) | ClusterIP + DNS |
| 용도 | 로컬 개발·테스트 | 프로덕션·스테이징 |
