// ============================================================================
// infra/load-test/auth-direct.js — k6 직접 부하 테스트 (auth-service 포트 8081)
// ============================================================================
// [무엇을] auth-service에 직접 부하. 게이트웨이(80) 우회. Caddy 포트 충돌 없이 실측.
// [왜] 로컬 개발 환경에서 Caddy(HTTPS)가 port 80을 점유해 게이트웨이 경유 테스트가 불가할 때.
//       auth-service 자체 성능(JWT 발급 TPS/p95)을 직접 측정.
// [실행] k6 run infra/load-test/auth-direct.js
// [게이트웨이 포함 테스트] 포트 충돌 해소 후 auth-login.js 로 전환.
// ============================================================================

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },   // 워밍업: 20 VU
    { duration: '1m',  target: 100 },  // 부하 상승: 100 VU
    { duration: '1m',  target: 100 },  // 유지: 100 VU
    { duration: '30s', target: 0 },    // 쿨다운
  ],
  thresholds: {
    // [SLO] p95 < 500ms, 에러율 < 1%
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

// [왜] auth-service 직접. 게이트웨이 경유(/api/auth/login) vs 직접(/auth/login) 경로 차이.
const BASE = __ENV.BASE_URL || 'http://localhost:8081';

export default function () {
  const payload = JSON.stringify({ username: 'admin', password: 'admin123' });
  const params = { headers: { 'Content-Type': 'application/json' } };

  // [왜] 게이트웨이 없이 직접 호출 → /auth/login (nginx가 /api/ 접두어 제거 불필요)
  const res = http.post(`${BASE}/auth/login`, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'has token': (r) => {
      try { const b = JSON.parse(r.body); return b.data && b.data.token !== undefined; }
      catch(e) { return r.body && r.body.includes('token'); }
    },
  });

  sleep(1);
}
