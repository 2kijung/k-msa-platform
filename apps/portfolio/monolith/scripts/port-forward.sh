#!/usr/bin/env bash
# 포트포워드 + Grafana 접근 스크립트
# 사용법: ./scripts/port-forward.sh [start|stop|status]

ACTION="${1:-start}"

FRONTEND_PORT=8888
BACKEND_PORT=8889
GRAFANA_PORT=3000
PROMETHEUS_PORT=9091

case "$ACTION" in
  start)
    echo "포트포워드 시작..."

    # 기존 종료
    kill $(lsof -ti:$FRONTEND_PORT,$BACKEND_PORT,$GRAFANA_PORT,$PROMETHEUS_PORT 2>/dev/null) 2>/dev/null || true
    sleep 1

    kubectl port-forward --address 0.0.0.0 -n portfolio   svc/portfolio-frontend                    $FRONTEND_PORT:80   > /dev/null 2>&1 &
    kubectl port-forward --address 0.0.0.0 -n portfolio   svc/portfolio-backend                     $BACKEND_PORT:8080  > /dev/null 2>&1 &
    kubectl port-forward --address 0.0.0.0 -n monitoring  svc/kube-prometheus-stack-grafana         $GRAFANA_PORT:80    > /dev/null 2>&1 &
    kubectl port-forward --address 0.0.0.0 -n monitoring  svc/kube-prometheus-stack-prometheus      $PROMETHEUS_PORT:9090 > /dev/null 2>&1 &
    sleep 2

    echo ""
    echo "  ┌──────────────────────────────────────────┐"
    echo "  │  서비스 접근 URL                           │"
    echo "  ├──────────────────────────────────────────┤"
    echo "  │  Portfolio  http://localhost:$FRONTEND_PORT       │"
    echo "  │  Backend    http://localhost:$BACKEND_PORT/api    │"
    echo "  │  Grafana    http://localhost:$GRAFANA_PORT         │"
    echo "  │             admin / grafana1234            │"
    echo "  │  Prometheus http://localhost:$PROMETHEUS_PORT       │"
    echo "  └──────────────────────────────────────────┘"
    echo ""
    # 헬스체크
    sleep 1
    STATUS=$(curl -s http://localhost:$BACKEND_PORT/api/auth/health 2>/dev/null | python3 -c "import sys,json; print(json.load(sys.stdin)['data'])" 2>/dev/null)
    echo "  Backend health: ${STATUS:-연결 실패}"
    echo "  Frontend: $(curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:$FRONTEND_PORT/ 2>/dev/null)"
    ;;

  stop)
    kill $(lsof -ti:$FRONTEND_PORT,$BACKEND_PORT,$GRAFANA_PORT,$PROMETHEUS_PORT 2>/dev/null) 2>/dev/null || true
    echo "포트포워드 종료"
    ;;

  status)
    for PORT in $FRONTEND_PORT $BACKEND_PORT $GRAFANA_PORT $PROMETHEUS_PORT; do
      PID=$(lsof -ti:$PORT 2>/dev/null)
      [ -n "$PID" ] && echo "  :$PORT → 실행 중 (PID: $PID)" || echo "  :$PORT → 중지"
    done
    ;;
esac
