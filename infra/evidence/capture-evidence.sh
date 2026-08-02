#!/bin/bash
# ============================================================================
# infra/evidence/capture-evidence.sh — 포트폴리오/자소서용 증거 자동 캡처
# ============================================================================
# [무엇을] k-msa-platform 운영 중인 서비스 상태를 파일로 기록한다.
# [왜] 자소서에 "현재 운영 중"이라 적어두었으므로, 실제 운영 증거를 주기적으로 남긴다.
# [어떻게] docker ps·k8s·curl 응답·k6 결과를 타임스탬프 파일로 저장.
# [실행] chmod +x infra/evidence/capture-evidence.sh && ./infra/evidence/capture-evidence.sh
# ============================================================================

TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")
OUTDIR="infra/evidence/${TIMESTAMP}"
mkdir -p "$OUTDIR"

echo "=== k-msa-platform 운영 증거 캡처: ${TIMESTAMP} ==="

# 1. Docker 컨테이너 현황
echo "[1/6] Docker 컨테이너 현황..."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" > "$OUTDIR/01_docker-ps.txt"
cat "$OUTDIR/01_docker-ps.txt"

# 2. 전체 서비스 Health 체크
echo "[2/6] 서비스 Health 체크..."
{
  echo "=== auth-service ===" && curl -s http://localhost:8081/auth/health
  echo "" && echo "=== content-service ===" && curl -s http://localhost:8084/content/health
  echo "" && echo "=== contact-service ===" && curl -s http://localhost:8085/contacts/health
  echo "" && echo "=== blog-service ===" && curl -s http://localhost:8086/blog/health
  echo "" && echo "=== analytics-service ===" && curl -s http://localhost:8087/analytics/health
  echo "" && echo "=== notification-service ===" && curl -s http://localhost:8083/notify/health
  echo "" && echo "=== gateway auth (port 8090) ===" && curl -s http://localhost:8090/api/auth/health
} > "$OUTDIR/02_service-health.txt" 2>&1
cat "$OUTDIR/02_service-health.txt"

# 3. Prometheus 스크레이프 현황
echo "[3/6] Prometheus 타겟 상태..."
curl -s http://localhost:9091/api/v1/targets | python3 -c "
import sys,json
d=json.load(sys.stdin)
for t in sorted(d['data']['activeTargets'], key=lambda x: x['labels']['job']):
    status='UP  ' if t['health']=='up' else 'DOWN'
    print(f'[{status}] {t[\"labels\"][\"job\"]:30} {t[\"labels\"][\"instance\"]}')
" > "$OUTDIR/03_prometheus-targets.txt" 2>&1
cat "$OUTDIR/03_prometheus-targets.txt"

# 4. Kubernetes 운영 상태 (포트폴리오 K8s 클러스터)
echo "[4/6] Kubernetes pods..."
kubectl get pods -n portfolio 2>&1 > "$OUTDIR/04_k8s-pods.txt" || echo "kubectl 접근 불가" > "$OUTDIR/04_k8s-pods.txt"
cat "$OUTDIR/04_k8s-pods.txt"

# 5. content-service 실제 데이터 확인
echo "[5/6] content-service 프로젝트 목록..."
curl -s http://localhost:8084/content/projects | python3 -c "
import sys,json
d=json.load(sys.stdin)
for p in d:
    print(f'[{p[\"id\"]}] {p[\"title\"]}')
    print(f'     Technologies: {p[\"technologies\"]}')
    print()
" > "$OUTDIR/05_content-projects.txt" 2>&1
cat "$OUTDIR/05_content-projects.txt"

# 6. Grafana 대시보드 목록
echo "[6/6] Grafana 대시보드..."
curl -s http://admin:admin@localhost:3000/api/search | python3 -c "
import sys,json
d=json.load(sys.stdin)
for item in d:
    print(f'[{item[\"type\"]}] {item[\"title\"]} (uid: {item.get(\"uid\",\"n/a\")})')
" > "$OUTDIR/06_grafana-dashboards.txt" 2>&1
cat "$OUTDIR/06_grafana-dashboards.txt"

echo ""
echo "=== 캡처 완료: ${OUTDIR}/ ==="
echo "파일 목록:"
ls -la "$OUTDIR/"
