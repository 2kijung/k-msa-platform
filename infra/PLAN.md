# infra/ — 환경·배포 확장 ★ (배포 확장의 핵심)

> @PLAN  dev→staging→prod, 반복 배포, 클러스터 코드화. K-portfolio의 K8s/CI 자산을 플랫폼 규모로 확장.
> @FROM  K-portfolio: k8s/(NetworkPolicy·PDB·ResourceQuota·monitoring), Jenkinsfile, helm/values.yaml
> @HOW   기존 단일 앱 배포 → 멀티서비스·멀티환경 배포로 일반화.
> @PHASE:6 (0에서 기초)
> @SCALE deploy
> @LINK  ../platform/observability/PLAN.md
> @LINK  ../docs/scalability.md#축-5-환경배포-확장

---

## 구성 (채울 자리)
- `helm/`          서비스 공통 차트 + values 오버라이드 (K-portfolio helm/values.yaml 확장)
- `environments/`  dev / staging / prod 값 분리
- `argocd/`        GitOps — git이 배포 상태의 단일 진실
- `terraform/`     IaC — 클러스터/네트워크/DB 코드화

## 이미 있는 강점 (K-portfolio에서 계승)
- NetworkPolicy 3종(격리), PDB, ResourceQuota, Prometheus+Grafana, Jenkins 완결형 파이프라인
> @DEEP 이것들을 "단일 앱" → "플랫폼(다중 서비스)" 스코프로 승격

<!--
@DEEP 채울 것:
  - Jenkins → 서비스별 변경 감지 빌드(monorepo path filter)
  - 무중단 배포(rolling/blue-green) 데모
  - @RISK 멀티환경 시크릿 관리(Sealed Secrets/Vault)
  - 면접 질문 5번(독립 배포·롤백)의 근거
-->
