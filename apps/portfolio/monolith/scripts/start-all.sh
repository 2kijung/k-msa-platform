#!/usr/bin/env bash
# K-Portfolio 전체 인프라 시작 스크립트
# 사용법: ./scripts/start-all.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JENKINS_HOME="/Users/k/.jenkins-portfolio"
JENKINS_WAR="/opt/homebrew/opt/jenkins-lts/libexec/jenkins.war"
JENKINS_PID_FILE="/tmp/jenkins-portfolio.pid"
JENKINS_LOG="/tmp/jenkins-portfolio.log"

echo_step() { echo ""; echo "▶ $1"; }
check_ok()   { echo "  ✓ $1"; }
check_warn() { echo "  ⚠ $1"; }

echo "=================================="
echo "  K-Portfolio 인프라 시작"
echo "=================================="

# ─────────────────────────────────────
echo_step "1. Docker Desktop"
# ─────────────────────────────────────
if docker info &>/dev/null; then
  check_ok "Docker Desktop 이미 실행 중"
else
  echo "  Docker Desktop 시작 중..."
  open -a Docker
  for i in $(seq 1 30); do
    sleep 3
    docker info &>/dev/null && break
    echo "  대기 중... ($i/30)"
  done
  docker info &>/dev/null && check_ok "Docker Desktop 시작 완료" || { echo "  ✗ Docker Desktop 시작 실패. 수동으로 실행하세요."; exit 1; }
fi

# ─────────────────────────────────────
echo_step "2. Minikube"
# ─────────────────────────────────────
MINIKUBE_STATUS=$(minikube status --format='{{.Host}}' 2>/dev/null || echo "Stopped")
if [ "$MINIKUBE_STATUS" = "Running" ]; then
  check_ok "Minikube 이미 실행 중 (IP: $(minikube ip 2>/dev/null))"
else
  echo "  Minikube 시작 중..."
  minikube start --driver=docker --cpus=4 --memory=5500 2>&1 | grep -E "Done|Ready|Error|끝났습니다" | tail -3
  check_ok "Minikube 시작 완료 (IP: $(minikube ip 2>/dev/null))"
fi

# ─────────────────────────────────────
echo_step "3. Kubernetes 리소스 확인 및 적용"
# ─────────────────────────────────────
kubectl apply -f "$PROJECT_ROOT/k8s/namespace.yaml"       2>/dev/null
kubectl apply -f "$PROJECT_ROOT/k8s/configmap.yaml"       2>/dev/null
kubectl apply -f "$PROJECT_ROOT/k8s/secret.yaml"          2>/dev/null
kubectl apply -f "$PROJECT_ROOT/k8s/postgres.yaml"        2>/dev/null
kubectl apply -f "$PROJECT_ROOT/k8s/resource-quota.yaml"  2>/dev/null
kubectl apply -f "$PROJECT_ROOT/k8s/pdb.yaml"             2>/dev/null

# 이미지 로드 (있을 때만)
if docker image inspect portfolio-backend:latest &>/dev/null; then
  minikube image load portfolio-backend:latest 2>/dev/null &
  minikube image load portfolio-frontend:latest 2>/dev/null &
  wait
  check_ok "Docker 이미지 Minikube에 로드"
else
  check_warn "Docker 이미지 없음 - Jenkins 파이프라인 먼저 실행 필요"
fi

# 배포 적용
sed 's|YOUR_DOCKERHUB_USERNAME/portfolio-backend:IMAGE_TAG|portfolio-backend:latest|g' \
  "$PROJECT_ROOT/k8s/backend.yaml" \
  | sed 's|imagePullPolicy: Always|imagePullPolicy: IfNotPresent|g' \
  | kubectl apply -n portfolio -f - 2>/dev/null

sed 's|YOUR_DOCKERHUB_USERNAME/portfolio-frontend:IMAGE_TAG|portfolio-frontend:latest|g' \
  "$PROJECT_ROOT/k8s/frontend.yaml" \
  | sed 's|imagePullPolicy: Always|imagePullPolicy: IfNotPresent|g' \
  | kubectl apply -n portfolio -f - 2>/dev/null

kubectl apply -f "$PROJECT_ROOT/k8s/ingress.yaml" 2>/dev/null
check_ok "K8s 리소스 적용 완료"

# ─────────────────────────────────────
echo_step "4. Jenkins"
# ─────────────────────────────────────
if [ -f "$JENKINS_PID_FILE" ]; then
  JENKINS_PID=$(cat "$JENKINS_PID_FILE")
  if kill -0 "$JENKINS_PID" 2>/dev/null; then
    check_ok "Jenkins 이미 실행 중 (PID: $JENKINS_PID, http://localhost:9090)"
  else
    rm -f "$JENKINS_PID_FILE"
  fi
fi

if [ ! -f "$JENKINS_PID_FILE" ]; then
  echo "  Jenkins 시작 중..."
  nohup /opt/homebrew/opt/openjdk@21/bin/java \
    -Djenkins.install.runSetupWizard=false \
    -DJENKINS_HOME="$JENKINS_HOME" \
    -jar "$JENKINS_WAR" \
    --httpPort=9090 \
    --httpListenAddress=127.0.0.1 \
    > "$JENKINS_LOG" 2>&1 &
  echo $! > "$JENKINS_PID_FILE"

  # 시작 대기
  for i in $(seq 1 30); do
    sleep 3
    if curl -s -u admin:admin1234 -o /dev/null -w "%{http_code}" http://localhost:9090/api/json 2>/dev/null | grep -q "200"; then
      check_ok "Jenkins 시작 완료 (http://localhost:9090)"
      break
    fi
    echo "  대기 중... ($i/30)"
  done
fi

# ─────────────────────────────────────
echo_step "5. 완료 요약"
# ─────────────────────────────────────
echo ""
echo "  ┌─────────────────────────────────────────────┐"
echo "  │  접근 URL                                    │"
echo "  ├─────────────────────────────────────────────┤"
echo "  │  Jenkins   http://localhost:9090              │"
echo "  │            admin / admin1234                  │"
echo "  ├─────────────────────────────────────────────┤"
echo "  │  Portfolio (port-forward 필요)               │"

FRONTEND_PORT=8888
BACKEND_PORT=8889

# port-forward 백그라운드 실행
kubectl port-forward -n portfolio svc/portfolio-frontend $FRONTEND_PORT:80  > /dev/null 2>&1 &
kubectl port-forward -n portfolio svc/portfolio-backend  $BACKEND_PORT:8080 > /dev/null 2>&1 &

echo "  │  Frontend  http://localhost:$FRONTEND_PORT              │"
echo "  │  Backend   http://localhost:$BACKEND_PORT/api/auth/health│"
echo "  ├─────────────────────────────────────────────┤"
echo "  │  Grafana   kubectl port-forward -n monitoring │"
echo "  │            svc/kube-prometheus-stack-grafana  │"
echo "  │            3000:80 → http://localhost:3000    │"
echo "  │            admin / grafana1234               │"
echo "  ├─────────────────────────────────────────────┤"
echo "  │  K8s 대시보드: minikube dashboard            │"
echo "  └─────────────────────────────────────────────┘"
echo ""
echo "  포트포워드 종료: kill \$(lsof -ti:$FRONTEND_PORT,$BACKEND_PORT)"
