# apps/portfolio — K-portfolio 흡수 기록 (Strangler Fig 출발점)

> @PLAN  monolith/(K-portfolio As-Is)를 게이트웨이 뒤에 두고, 도메인을 하나씩 서비스로 분리.
> @FROM  C:\dev\portfolio (github.com/2kijung/K-portfolio), `git archive`로 흡수 (2026-07-24)
> @HOW   추적 파일만 복사 → 빌드산출물(node_modules/target/logs) 자연 제외. **개인정보 PDF(이력서·자소서)는 의도적으로 제외.**
> @LINK  content-service/PLAN.md · contact-service/PLAN.md · analytics-service/PLAN.md (분리 목표)

---

## 현재 구조
```
apps/portfolio/
├── monolith/            ← K-portfolio As-Is (전환 전 기준선)
│   ├── dev-portfolio/          React 프론트
│   ├── dev-portfolio-backend/  Spring Boot 3.2 백엔드 (10개 도메인)
│   ├── k8s/ jenkins/ scripts/  인프라 (추후 ../../infra 로 이동)
│   └── *.md                    문서
├── content-service/PLAN.md     분리 목표: Project/Career/Skill/Certification/Profile
├── contact-service/PLAN.md     분리 목표: Contact + 알림 연동
└── analytics-service/PLAN.md   분리 목표: Visitor 통계
```

## @DEEP 다음 단계 (Strangler Fig)
1. **게이트웨이 라우팅**: `platform/gateway`에서 monolith로 통째 라우팅 → 무손상 기준선 확보
2. **점진 분리**: 도메인 하나씩 서비스로 떼어내고, monolith에서 해당 코드 제거 (각 단계 = 커밋 = 전환 서사)
3. **인프라 이동**: monolith/k8s·jenkins·scripts → `infra/`로 승격(멀티서비스 스코프)
4. **JD/자소서 정합**: 분리 후 platform 계층에 실제 구현 추가
   - Service Discovery, Kafka(messaging), Redis 캐시, 부하테스트(대용량 대비) — 자소서가 "사실"이 되도록
