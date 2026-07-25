// ============================================================================
// infra/load-test/auth-login.js — k6 부하 테스트 (auth-service 로그인)
// ============================================================================
// [무엇을] auth-service의 /auth/login에 부하를 주어, 초당 처리량(TPS)과 응답시간(p95),
//          에러율을 측정한다. "대규모 트래픽 환경 검증"의 실제 근거를 만든다.
// [왜/이력] 자소서·JD의 "대용량 트래픽"을 "말"이 아니라 "수치"로 증명하기 위함.
//           before/after(예: HPA 적용 전후) 비교로 오토스케일 효과까지 보여줄 수 있다.
// [어떻게 실행]
//   1) infra에서 docker compose up 으로 서비스 기동
//   2) k6 설치 후:  k6 run infra/load-test/auth-login.js
//   3) 결과의 http_req_duration(p95), http_reqs(TPS), http_req_failed(에러율) 확인
// [주의] 부하 대상은 게이트웨이(80) 경유 or auth 직접(8081). 여기선 게이트웨이 경유로 실전에 가깝게.
// ============================================================================

import http from 'k6/http';
import { check, sleep } from 'k6';

// [무엇을] 부하 프로파일. 계단식으로 가상 사용자(VU)를 늘려 한계 지점을 관찰한다.
export const options = {
  stages: [
    { duration: '30s', target: 20 },   // [왜] 워밍업: 20명까지 서서히
    { duration: '1m',  target: 100 },  // [왜] 부하 상승: 100명 동시
    { duration: '1m',  target: 100 },  // [왜] 유지: 100명에서 안정성 관찰
    { duration: '30s', target: 0 },    // [왜] 쿨다운
  ],
  thresholds: {
    // [왜/SLO] 통과 기준(서비스 수준 목표). 이 선을 넘으면 테스트 실패로 표시 → 성능 회귀 감지.
    http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이내
    http_req_failed: ['rate<0.01'],    // 에러율 1% 미만
  },
};

// [왜] 게이트웨이 단일 진입점으로 요청(실전과 동일 경로). 환경변수로 대상 주소 변경 가능.
const BASE = __ENV.BASE_URL || 'http://localhost';

export default function () {
  const payload = JSON.stringify({ username: 'admin', password: 'admin123' });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const res = http.post(`${BASE}/api/auth/login`, payload, params);

  // [무엇을] 응답 검증. 로그인 성공(200) + 토큰 존재 확인.
  check(res, {
    'status is 200': (r) => r.status === 200,
    'has token': (r) => {
      try { const b = JSON.parse(r.body); return b.data && b.data.token !== undefined; }
      catch(e) { return r.body && r.body.includes('token'); }
    },
  });

  sleep(1);  // [왜] 실제 사용자처럼 요청 간 간격
}

// @DEEP 채울 것:
//   - HPA 적용 전/후 결과를 README에 표로 (VU 100에서 TPS·p95 비교) → 자소서 "검증함" 근거
//   - 문의 접수(/api/contacts) 시나리오 추가 (쓰기 부하)
//   - 결과 그래프(k6 → Grafana k6 대시보드 or 출력 캡처) 스크린샷
