#!/usr/bin/env bash
# 포트포워드 자동 재연결 데몬
# LaunchAgent 환경에서 실행되므로 절대경로 사용

export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin"
export KUBECONFIG="/Users/k/.kube/config"

KUBECTL="/opt/homebrew/bin/kubectl"
NAMESPACE="portfolio"
FRONTEND_PORT=8888
BACKEND_PORT=8889

log() { echo "[$(date '+%H:%M:%S')] $1"; }

forward_frontend() {
  while true; do
    log "frontend 포트포워드 시작 (:${FRONTEND_PORT})"
    $KUBECTL port-forward --address 0.0.0.0 -n "$NAMESPACE" svc/portfolio-frontend "${FRONTEND_PORT}:80" 2>/tmp/pf-frontend-err.log
    log "frontend 끊김($(cat /tmp/pf-frontend-err.log 2>/dev/null | tail -1)) — 5초 후 재연결"
    sleep 5
  done
}

forward_backend() {
  while true; do
    log "backend 포트포워드 시작 (:${BACKEND_PORT})"
    $KUBECTL port-forward --address 0.0.0.0 -n "$NAMESPACE" svc/portfolio-backend "${BACKEND_PORT}:8080" 2>/tmp/pf-backend-err.log
    log "backend 끊김($(cat /tmp/pf-backend-err.log 2>/dev/null | tail -1)) — 5초 후 재연결"
    sleep 5
  done
}

log "포트포워드 데몬 시작"
forward_frontend &
forward_backend &
wait
