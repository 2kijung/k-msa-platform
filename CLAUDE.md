# CLAUDE.md — 이 리포에서 작업하는 AI(Claude Code)를 위한 안내

> 이 파일은 Claude Code가 세션 시작 시 자동으로 읽는다. 다른 컴퓨터에서 clone 후
> 켜면 이 파일로 맥락을 파악하고 **바로 이어서 작업**할 수 있어야 한다.
> 사람이 보는 상세 로드맵은 `INTEGRATION_PLAN.md`, 설계는 `MIGRATION_DESIGN.md`.

---

## 이 프로젝트가 뭔가

**모놀리스(K-portfolio) → MSA 전환 플랫폼.** 개인 이직(백엔드 경력)용 포트폴리오.
커밋 히스토리 자체가 "모놀리스를 어떻게 점진적으로 쪼갰는지"의 전환 서사다.

- 목적: MSA/DevOps 역량을 채용담당자·면접에서 증명. 서사를 중시.
- 사용자: 4년차 SI 백엔드(제조·공공·금융, 대기업 대규모 시스템). 이직 목표 = 대용량 서비스 회사.
- 구조: `platform/`(공유 계층: gateway·auth·notification·observability·messaging·service-template)
        + `apps/`(개별 앱: portfolio·budget·blog) + `infra/`(compose·부하테스트·배포) + `docs/`

## 작업 규칙 (반드시 지킬 것)

1. **모든 코드에 "뭘/어떻게/왜 + MSA 근거" 주석을 단다.** 나중에 주석만 보고 이해·면접설명 가능하게.
   마커: `@PLAN @FROM @HOW @DEEP(나중에 채울 자리) @LINK @SCALE @RISK`. 미완성 추적: `grep -rn "@DEEP" .`
2. **작업할 때마다 `INTEGRATION_PLAN.md`의 §5 로드맵 상태 + §6 진행현황/Changelog를 실시간 갱신한다.**
   착수 🔵 / 완료 ✅(날짜) / 계획에 없던 게 생기면 Changelog에 "왜"와 함께.
3. **커밋 메시지 형식**: 첫 줄 `YYYY-MM-DD K`, 이후 작업 내역. 끝에 Co-Authored-By 트레일러 유지.
4. **스캐폴딩 우선 → 점진 심화.** 전체 뼈대+주석 먼저, 이후 @DEEP를 따라 채우고 연결.
5. **DB는 실물 PostgreSQL**(dev-prod parity). H2는 테스트 전용. 서비스마다 스키마 분리(auth/contact/...).
6. **폴리글랏 실험 OK**이나 신규 서비스는 기본 Spring Boot 3.2. 서비스 간은 REST/이벤트로만 통신(코드 JAR 공유 금지).

## 지금까지 완료 (2026-08-02 기준)

- **Phase 0** ✅ K-portfolio 모놀리스 흡수 → `apps/portfolio/monolith/`
- **Phase 1** ✅ auth-service 완전 분리 (로그인·JWT·/auth/verify) + 게이트웨이 + monolith에서 auth 제거
- **Phase 2** ✅ contact-service + notification-service (문의→알림, REST) + Dockerfile + Resilience4j 서킷브레이커
- **Phase 3** ✅ content-service (엔티티 5종·CRUD·DataInitializer) — 기동 검증 완료
- **Phase 4** 🔵 blog-service (BlogPost·예약발행 스케줄러·PostingClient dry-run) — @DEEP Tistory/Velog 실 API
- **Phase 5** 🔵 analytics-service (Visitor 기록·통계) — 기동 검증 완료
- **Phase 6** 🔵 진행 중
  - ✅ k6 실측: auth-direct TPS 44.7 p95 744ms / gateway TPS 42.9 p95 1.29s / 에러율 0%
  - ✅ Prometheus 6서비스 ALL UP (auth·content·contact·blog·analytics·notification)
  - ✅ Grafana 대시보드 자동 프로비저닝 (TPS·p95·에러율·서킷브레이커 패널)
  - ✅ Resilience4j 서킷브레이커 (contact→notification, CLOSED/OPEN/HALF-OPEN)
  - ⬜ Zipkin 분산 트레이싱 (@DEEP)
- ✅ **Jenkinsfile**: 6서비스 병렬 빌드·테스트·Docker·K8s Rolling Update
- ✅ **증거 캡처**: `infra/evidence/capture-evidence.sh`
- ✅ **k-devops.duckdns.org** (K-portfolio 사이트): k-msa-platform 프로젝트 추가됨 (id=9)

## ▶ 다음 세션 시작점

**Phase 6 실탄 계속:**
1. `cd infra && docker compose up -d --build` → 컨테이너 기동 (name: k-msa-platform 고정)
2. k6 게이트웨이 경유 재측: `k6 run infra/load-test/auth-login.js` (gateway 포트 8090)
3. Grafana(localhost:3000) → Prometheus 데이터소스 → 서비스별 패널 스크린샷
4. Zipkin 분산 트레이싱 착수
5. Resilience4j — contact→notification 호출에 타임아웃/서킷브레이커

**그다음:**
- Phase 4: blog-service 통합
- Phase 5: analytics-service + budget 편입

**이미 완료된 것 (재작업 금지):**
- Phase 0~3: 모두 기동 검증 완료
- k6 auth-service 직접 실측: TPS 44.7, p95 744ms, 에러율 0% (8,081건)

**포트 주의 (로컬 환경):**
- gateway: 8090 (Caddy가 80/443 점유 — k-devops.duckdns.org)
- auth-service: 8081 (직접), /api/auth/* (gateway 경유)
- content-service: 8084
- contact-service: 8085
- openclaw-msa compose: name=openclaw-msa, ports 8083/8082/8081/8089

## 자기소개서 (네이버웹툰 등)

- **실제 자소서 파일은 이 리포에 없다**(개인정보). `C:\dev\naver-webtoon-자기소개서.txt` 로컬 보관.
  새 PC에서 자소서를 이어가려면 그 파일을 USB/클라우드로 따로 옮겨야 함.
- 진행 상황·전략·잡아낸 허위(재발 방지)는 `INTEGRATION_PLAN.md` §7 에 기록됨.
- 자소서 원칙: 사고방식 중심 / 문항별 역할 분담 / **사실만(코드와 대조 검증)** / 약점 먼저 말하지 않기.
- ⚠️ 자소서에 프로젝트 기술을 쓸 땐 반드시 실제 코드와 대조. 과거 허위 사례:
  "Service Discovery"(없음→DNS), "Boot Auto Config 이해"(openclaw는 Boot 아님→직접배선),
  "대용량 트래픽"(근거 없음→대량 데이터 40만건 등으로 대체).

## 관련 리포·자산

- 원본(참조용, 건드리지 않음): `C:\openclaw-msa`(MSA 실험작), `C:\dev\portfolio`(K-portfolio 원본)
- 이 리포: github.com/2kijung/k-msa-platform
