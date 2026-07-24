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

## 지금까지 완료 (2026-07-24 기준)

- **Phase 0** ✅ K-portfolio 모놀리스 흡수 → `apps/portfolio/monolith/`
- **Phase 1** ✅ auth-service 완전 분리 (로그인·JWT·/auth/verify) + 게이트웨이 + monolith에서 auth 제거
- **Phase 2** ✅ contact-service + notification-service (문의→알림, REST)
- **Phase 6** 🔵 착수 — 부하테스트(k6) + 관측성(Prometheus/Grafana) **코드만** 작성, 실측정 대기

## ▶ 다음 세션 시작점 (clone/pull 후 여기부터)

**최우선: Phase 6 실탄 완성 — 자소서의 "대용량 트래픽" 갭을 실측 수치로 메우는 작업.**

1. `cd infra && docker compose up -d --build` → 8개 컨테이너 기동 확인
   (postgres·auth·monolith·gateway·contact·notification·prometheus·grafana)
2. `k6 run infra/load-test/auth-login.js` → `infra/load-test/README.md` 측정표에 TPS/p95/에러율 기록
3. Grafana(localhost:3000, admin/admin) → Prometheus 연결 → 서비스별 패널 → 스크린샷
4. 분산 트레이싱(Zipkin) 착수 — `platform/observability/PLAN.md` 참고
5. 회복탄력성(Resilience4j) — contact→notification 호출에 타임아웃/서킷브레이커

그다음: Phase 3(content 분리) → Phase 4(blog) → Phase 5(analytics+budget).
상세·이유는 `INTEGRATION_PLAN.md` §6 참조.

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
