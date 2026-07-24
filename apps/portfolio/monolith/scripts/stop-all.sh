#!/usr/bin/env bash
# K-Portfolio 전체 인프라 중지

JENKINS_PID_FILE="/tmp/jenkins-portfolio.pid"

echo "=================================="
echo "  K-Portfolio 인프라 중지"
echo "=================================="

# Jenkins 종료
if [ -f "$JENKINS_PID_FILE" ]; then
  JENKINS_PID=$(cat "$JENKINS_PID_FILE")
  if kill -0 "$JENKINS_PID" 2>/dev/null; then
    kill "$JENKINS_PID"
    echo "  ✓ Jenkins 종료 (PID: $JENKINS_PID)"
  fi
  rm -f "$JENKINS_PID_FILE"
fi

# 포트포워드 종료
kill $(lsof -ti:8888,8889,9090 2>/dev/null) 2>/dev/null || true
echo "  ✓ 포트포워드 종료"

# Minikube 중지 (선택)
echo ""
read -r -p "  Minikube도 종료할까요? (y/N) " ans
if [[ "$ans" =~ ^[Yy]$ ]]; then
  minikube stop
  echo "  ✓ Minikube 종료"
fi

# Docker Desktop 종료 (선택)
read -r -p "  Docker Desktop도 종료할까요? (y/N) " ans
if [[ "$ans" =~ ^[Yy]$ ]]; then
  osascript -e 'quit app "Docker"'
  echo "  ✓ Docker Desktop 종료"
fi

echo ""
echo "완료."
