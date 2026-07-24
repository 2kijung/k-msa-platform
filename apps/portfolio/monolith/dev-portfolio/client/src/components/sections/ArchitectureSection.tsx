/* ==========================================================
   Architecture 섹션 — 이 포트폴리오 프로젝트의 실제 구조 (정직 버전)
   React(SPA) → REST/JWT → Spring Boot(계층) → JPA → H2/PostgreSQL
   ========================================================== */

import { useInView } from "@/hooks/useInView";
import { ArrowDown, Monitor, Server, Database } from "lucide-react";

const layers = [
  {
    icon: Monitor,
    title: "Client — React SPA",
    port: ":3000",
    desc: "브라우저에서 동작하는 단일 페이지 앱. 컴포넌트가 API로 데이터를 받아 렌더링.",
    tags: ["React 19", "Vite", "TypeScript", "Tailwind CSS", "Wouter"],
    color: "#61dafb",
  },
  {
    icon: Server,
    title: "Backend — Spring Boot",
    port: ":8081 /api",
    desc: "Controller(REST) → Service(비즈니스 로직) → Repository(JPA) 3계층. JWT 인증, 파일 업로드, 관리자 CMS 포함.",
    tags: ["Spring Boot 3.2", "Spring Security(JWT)", "Spring Data JPA", "REST API"],
    color: "#6db33f",
  },
  {
    icon: Database,
    title: "Database",
    port: "JPA / Hibernate",
    desc: "엔티티로 스키마 설계, JPA가 SQL 생성. 개발은 H2(파일), 배포 시 PostgreSQL로 URL만 교체.",
    tags: ["H2 (개발)", "PostgreSQL (배포)", "ddl-auto: update"],
    color: "#336791",
  },
];

const connectors = ["HTTP · REST(JSON) · JWT 토큰", "JPA / Hibernate (JDBC)"];

export default function ArchitectureSection() {
  const { ref, inView } = useInView();

  return (
    <section
      id="architecture"
      className="py-24 relative"
      style={{ background: "linear-gradient(180deg, #060b18 0%, #0a1628 50%, #060b18 100%)" }}
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`text-center mb-14 transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <p className="section-label mb-3">System Architecture</p>
          <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
            이 포트폴리오 <span className="gradient-text">시스템 구조</span>
          </h2>
          <p className="text-slate-500 mt-4 max-w-2xl mx-auto">
            React(프론트) ↔ Spring Boot(백엔드) ↔ JPA/DB 3계층. 도메인별로 계층을 분리한
            <b className="text-slate-300"> 모듈형 모놀리식</b>으로 설계해, 추후 특정 도메인을 서비스로 분리(MSA)할 수 있게 구성.
          </p>
        </div>

        <div className="grid lg:grid-cols-3 gap-8">
          {/* 계층 다이어그램 */}
          <div className="lg:col-span-2">
            <div className="flex flex-col items-center">
              {layers.map((l, i) => (
                <div key={l.title} className="w-full">
                  <div
                    className={`glass-card rounded-2xl p-5 w-full transition-all duration-700 ${
                      inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
                    }`}
                    style={{ transitionDelay: `${i * 120}ms`, borderColor: `${l.color}30` }}
                  >
                    <div className="flex items-center gap-3 mb-2">
                      <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                        style={{ background: `${l.color}18`, border: `1px solid ${l.color}40` }}
                      >
                        <l.icon className="w-5 h-5" style={{ color: l.color }} />
                      </div>
                      <div>
                        <div className="text-white font-bold">{l.title}</div>
                        <div className="text-xs font-mono text-slate-500">{l.port}</div>
                      </div>
                    </div>
                    <p className="text-sm text-slate-400 leading-relaxed mb-3">{l.desc}</p>
                    <div className="flex flex-wrap gap-1.5">
                      {l.tags.map((t) => (
                        <span key={t} className="text-xs px-2 py-0.5 rounded bg-white/5 text-slate-300 border border-white/10">
                          {t}
                        </span>
                      ))}
                    </div>
                  </div>
                  {i < layers.length - 1 && (
                    <div className="flex flex-col items-center py-2 text-slate-500">
                      <ArrowDown className="w-4 h-4" />
                      <span className="text-[11px] font-mono">{connectors[i]}</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* 요청 흐름 + 설계 포인트 */}
          <div className="space-y-4">
            <div className="glass-card rounded-2xl p-5">
              <h4 className="text-xs font-mono text-blue-400 mb-3 tracking-wider uppercase">요청 흐름 (로그인 예시)</h4>
              <div className="space-y-1.5 text-xs font-mono text-slate-400 leading-relaxed">
                <div>React <span className="text-slate-600">→</span> POST /api/auth/login</div>
                <div className="pl-3 text-slate-600">↓ JwtAuthenticationFilter</div>
                <div className="pl-3">AuthController <span className="text-slate-600">→</span> AuthService</div>
                <div className="pl-3">UserRepository <span className="text-slate-600">→</span> DB 조회 · BCrypt 검증</div>
                <div className="pl-3 text-green-400">↑ JWT 발급 → localStorage 저장</div>
              </div>
            </div>

            <div className="glass-card rounded-2xl p-5">
              <h4 className="text-xs font-mono text-blue-400 mb-3 tracking-wider uppercase">설계 포인트</h4>
              <ul className="space-y-2 text-sm text-slate-400">
                <li className="flex gap-2"><span className="text-blue-400">•</span> 도메인별 Controller-Service-Repository-Entity 계층 분리</li>
                <li className="flex gap-2"><span className="text-blue-400">•</span> 조회는 공개 / 쓰기·관리는 JWT 인증</li>
                <li className="flex gap-2"><span className="text-blue-400">•</span> JPA로 DB 교체 용이 (H2 → PostgreSQL)</li>
                <li className="flex gap-2"><span className="text-blue-400">•</span> 관리자 CMS로 콘텐츠 직접 관리</li>
              </ul>
            </div>

            <div className="glass-card rounded-2xl p-5 border border-yellow-500/15">
              <h4 className="text-xs font-mono text-yellow-400 mb-2 tracking-wider uppercase">배포 (예정)</h4>
              <p className="text-xs text-slate-400 leading-relaxed">
                Dockerfile · docker-compose 구성 완료. 향후 <b className="text-slate-300">PostgreSQL → Kubernetes → Jenkins CI/CD → AWS</b> 순으로 배포 예정.
              </p>
            </div>
          </div>
        </div>

        {/* MSA 전환 로드맵 (목표) */}
        <div className="mt-10 glass-card rounded-2xl p-6">
          <div className="flex items-center gap-2 mb-5">
            <h3 className="text-lg font-bold text-white">MSA 전환 로드맵</h3>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-yellow-400/10 text-yellow-400 border border-yellow-400/25">목표 / 계획</span>
          </div>
          <p className="text-sm text-slate-400 mb-6">
            현재는 모듈형 모놀리식. 도메인 계층을 이미 분리해 두어, 부하·독립성이 필요한 도메인부터
            <b className="text-slate-300"> 점진적으로(Strangler Fig)</b> 서비스로 떼어낼 계획.
          </p>

          {/* 현재 → 목표 */}
          <div className="grid md:grid-cols-2 gap-4 mb-6">
            <div className="rounded-xl border border-white/10 p-4">
              <div className="text-xs font-mono text-green-400 mb-2">현재 (AS-IS)</div>
              <div className="rounded-lg bg-white/5 border border-white/10 p-3 text-center text-sm text-slate-300">
                Spring Boot 모놀리식 1개<br /><span className="text-xs text-slate-500">인증 · 프로필 · 경력 · 프로젝트 · 자격증 (DB 1개)</span>
              </div>
            </div>
            <div className="rounded-xl border border-blue-500/20 p-4">
              <div className="text-xs font-mono text-blue-400 mb-2">목표 (TO-BE)</div>
              <div className="text-center text-xs text-slate-500 mb-2">API Gateway</div>
              <div className="flex gap-2 justify-center flex-wrap">
                {["인증 서비스", "프로필 서비스", "프로젝트 서비스"].map((s) => (
                  <div key={s} className="rounded-lg bg-blue-500/10 border border-blue-500/25 px-2.5 py-1.5 text-xs text-blue-200">
                    {s}<br /><span className="text-[10px] text-slate-500">+ 독립 DB</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 단계 */}
          <div className="flex flex-wrap gap-2">
            {[
              { n: "1", t: "모듈형 모놀리식", done: true },
              { n: "2", t: "인증 서비스 분리", done: false },
              { n: "3", t: "프로젝트/자격증 서비스 분리", done: false },
              { n: "4", t: "API Gateway + 서비스별 DB", done: false },
              { n: "5", t: "K8s 오케스트레이션", done: false },
            ].map((p) => (
              <div
                key={p.n}
                className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs border ${
                  p.done
                    ? "bg-green-500/10 border-green-500/30 text-green-300"
                    : "bg-white/5 border-white/10 text-slate-400"
                }`}
              >
                <span className="font-mono font-bold">{p.done ? "✓" : p.n}</span>
                {p.t}
                {p.done && <span className="text-[10px] text-green-400">(현재)</span>}
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
