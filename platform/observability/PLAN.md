# observability/ — 관측성·회복탄력성 (MSA의 진짜 "통합 관리" 계층) ★

> @PLAN  흩어진 서비스를 "하나처럼" 관측·제어. 이 프로젝트에서 점수 가장 높은 곳.
> @FROM  K-portfolio: k8s/monitoring/prometheus-values.yaml (Prometheus+Grafana 이미 구성됨!), Actuator/Micrometer 의존성
> @HOW   기존 모니터링을 전 서비스로 확장 + 분산 트레이싱 신규 + 회복탄력성 신규.
> @PHASE:6
> @LINK  ../gateway/PLAN.md              (트레이싱 헤더 전파 시작점)
> @LINK  ../services/contact-service/PLAN.md  (이벤트 기반 전환과 함께)
> @LINK  ../docs/interview-narrative.md  (면접 5대 질문 방어의 핵심 근거)

---

## 3대 축 (채울 자리)

### 1. 메트릭 — Prometheus + Grafana  [이미 절반 완료]
> @FROM K-portfolio prometheus-values.yaml → 전 서비스 scrape 대상 추가
> @DEEP Grafana 대시보드(서비스별 지연·에러율) → README 상단 스크린샷용

### 2. 분산 트레이싱 — Zipkin / Tempo  [신규, ★최우선]
> @PLAN traceId가 gateway→auth→content→... 서비스를 넘나드는 흐름 추적
> @HOW  Micrometer Tracing + OTel, W3C Trace Context 헤더 전파
> @DEEP 각 서비스 계측, Zipkin UI 스크린샷 → "분산 디버깅 할 줄 안다" 증명

### 3. 회복탄력성 — Resilience4j  [신규, 면접 방어]
> @PLAN 서비스 간 호출에 타임아웃·재시도·서킷브레이커·폴백
> @HOW  contact→notification, gateway→upstream 등에 적용
> @DEEP 서킷 오픈 시나리오 데모 → "장애 전파 막을 줄 안다" 증명

<!--
@DEEP 공통 로깅(Loki/ELK)로 중앙 로그 수집도 추가 검토.
      이 계층 전체가 "MSA를 통합 관리한다"의 실체. 코드 병합이 아니라 관측/제어로 통합.
-->
