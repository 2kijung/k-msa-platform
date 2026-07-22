# 확장성 설계 (Scalability & Extensibility) — 5축

> @PLAN  "개인 MSA 플랫폼"이 어떻게 커지는가. 서비스 6개에서 끝나지 않고 앱을 계속 얹는 골격.
> @LINK  ../INTEGRATION_PLAN.md §3   (5축 요약)
> @DEEP  각 축을 실제 구현/스크린샷과 연결하며 심화. 구현은 골격 우선(다 벌리지 말 것).

---

## 축 1. 서비스 확장 (Extensibility) — 새 앱을 어떻게 찍어내나 ★
> @SCALE service   @LINK ../platform/service-template/PLAN.md
- service-template: 새 서비스 = 템플릿 복제 + 이름만 (게이트웨이 등록·관측 계측·DB 스키마 자동 포함)
- 공통 라이브러리(platform layer): 표준 응답·에러·인증헤더 파서 (단, javax/jakarta 오염 없는 POJO만)
- API 계약: OpenAPI 스펙으로 서비스 간 계약 고정 → 독립 진화
> @DEEP 템플릿 실제 구조, 신규 서비스 추가 체크리스트

## 축 2. 트래픽 확장 (Scale-out)
> @SCALE traffic   @LINK ../infra/PLAN.md
- Stateless 서비스 → 수평복제 자유 (세션은 JWT로 무상태)
- K8s HPA(CPU/메모리/커스텀 메트릭 기반 오토스케일)
- 부하테스트(k6/Gatling)로 스케일 근거 확보 → "부하 경험 없다" 약점 보완
> @DEEP HPA 매니페스트, k6 시나리오, 부하 그래프

## 축 3. 데이터 확장
> @SCALE data   @LINK ../MIGRATION_DESIGN.md §4
- 캐시 계층(Redis) — 읽기 많은 조회(포트폴리오/방문통계)
- DB per service or 스키마 분리 (경계별 독립 스케일)
- 읽기 복제(read replica), 필요 시 CQRS(명령/조회 분리)
> @DEEP 캐시 대상 선정, 무효화 전략

## 축 4. 통신 확장 (결합도 ↓)
> @SCALE comm   @LINK ../platform/messaging/PLAN.md
- 동기 REST → 비동기 이벤트(Kafka/RabbitMQ)로 전환
- 예: contact→notification 을 "ContactCreated" 이벤트 발행/구독으로
- 이점: 서비스 추가가 기존 코드 수정 없이 이벤트 구독만으로 됨 (확장성 핵심)
> @DEEP 이벤트 카탈로그, 발행/구독 계약, 재처리/멱등성

## 축 5. 환경·배포 확장
> @SCALE deploy   @LINK ../infra/PLAN.md
- 멀티환경(dev/staging/prod) 값 분리
- Helm 차트화(서비스 공통 차트 + values 오버라이드)
- GitOps(ArgoCD) — git = 배포 상태의 단일 진실
- IaC(Terraform) — 클러스터/리소스 코드화
> @DEEP 환경별 values, ArgoCD App, Terraform 모듈

---

## ⚖️ 확장성의 함정 (넓게 보되 빠지지 말 것)
- **과확장(over-engineering) 재발**: 5축 다 구현 X. **설계로 증명 + 대표 1~2개만 실동작**.
- 확장성을 말로만 하면 감점. 각 축에 **최소 1개 "동작하는 증거"**(HPA 데모/이벤트 흐름/캐시 히트율)를 남긴다.
