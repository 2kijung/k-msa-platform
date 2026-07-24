/* ==========================================================
   About 섹션 — Profile 소개 + 실제 데이터 개수(경력/프로젝트/자격증/스킬)
   가짜 통계 대신 DB의 실제 개수를 집계해 보여줌
   ========================================================== */

import { useEffect, useState } from "react";
import { useInView } from "@/hooks/useInView";
import { Code2, Server, Cloud, Award, GraduationCap, Shield, MapPin, Cake } from "lucide-react";
import {
  profileApi, careerApi, projectApi, certificationApi, skillApi,
  type ProfileData,
} from "@/lib/api";

const PLACEHOLDER_IMG =
  "https://d2xsxph8kpxj0f.cloudfront.net/310519663398554856/hsrSa3YUTQvKpXz9DLCYxj/profile-abstract-4WH4AFT3yksx6QBfnqFHP9.webp";

const highlights = [
  { icon: "🔗", title: "시스템 연계 · 인터페이스", desc: "SGI 보증보험·SAP·카카오톡 등 외부 시스템 연계 및 인터페이스 API 개발" },
  { icon: "🏗️", title: "레거시 고도화 · 오너십", desc: "구형 시스템 고도화, 2차 프로젝트 단독 수행 경험" },
  { icon: "🐞", title: "장애 원인 규명", desc: "네트워크·보안·설정 계층을 하나씩 파고들어 근본 원인 특정 (트러블슈팅)" },
  { icon: "🏦", title: "다양한 도메인", desc: "은행·공기업·대기업 SI 프로젝트 (엄격한 보안·감사 환경 경험)" },
];

const interests = ["Spring Boot", "JPA", "React", "MSA 전환", "Docker / K8s", "CI/CD"];

export default function AboutSection() {
  const { ref, inView } = useInView();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [counts, setCounts] = useState({ careers: 0, projects: 0, certs: 0, skills: 0 });

  useEffect(() => {
    const load = async () => {
      const toArr = (d: any) => (Array.isArray(d) ? d : (d?.content ?? []));
      const [p, ca, pr, ce, sk] = await Promise.all([
        profileApi.get(), careerApi.getAll(), projectApi.getAll(),
        certificationApi.getAll(), skillApi.getAll(),
      ]);
      if (p.success && p.data) setProfile(p.data);
      setCounts({
        careers: toArr(ca.data).length,
        projects: toArr(pr.data).length,
        certs: toArr(ce.data).length,
        skills: toArr(sk.data).length,
      });
    };
    load();
  }, []);

  const stats = [
    { label: "경력", value: `${counts.careers}`, icon: Server },
    { label: "프로젝트", value: `${counts.projects}`, icon: Cloud },
    { label: "자격증", value: `${counts.certs}`, icon: Award },
    { label: "기술 스택", value: `${counts.skills}`, icon: Code2 },
  ];

  // 기본 정보 (값이 있는 것만) — 기존 '기본정보' 섹션을 About에 통합
  const edu = [profile?.university, profile?.major].filter(Boolean).join(" ");
  const basicFacts = [
    edu && { icon: GraduationCap, label: "학력", value: `${edu}${profile?.graduationStatus ? ` (${profile.graduationStatus})` : ""}` },
    profile?.militaryStatus && { icon: Shield, label: "병역", value: profile.militaryStatus },
    profile?.location && { icon: MapPin, label: "거주지", value: profile.location },
    profile?.birthDate && { icon: Cake, label: "생년", value: profile.birthDate },
  ].filter(Boolean) as { icon: any; label: string; value: string }[];

  return (
    <section id="about" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-blue-950/5 to-transparent pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`grid lg:grid-cols-2 gap-8 lg:gap-16 items-start transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          {/* Left: Profile visual */}
          <div className="relative">
            <div className="relative w-full max-w-sm mx-auto lg:mx-0">
              <div className="aspect-[3/4] rounded-2xl overflow-hidden neon-border">
                <img
                  src={profile?.imageUrl || PLACEHOLDER_IMG}
                  alt="Developer Profile"
                  className="w-full h-full object-cover"
                />
              </div>
            </div>

            {/* Stats grid — 실제 데이터 개수 */}
            <div className="grid grid-cols-2 gap-3 mt-8 max-w-sm mx-auto lg:mx-0">
              {stats.map(({ label, value, icon: Icon }) => (
                <div key={label} className="glass-card p-4 rounded-xl text-center">
                  <Icon className="w-5 h-5 text-blue-400 mx-auto mb-2" />
                  <div className="text-2xl font-extrabold text-white mb-1" style={{ fontFamily: "Sora, sans-serif" }}>
                    {value}
                  </div>
                  <div className="text-xs text-slate-500">{label}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Right: Bio */}
          <div>
            <p className="section-label mb-3">About Me</p>
            <h2 className="text-4xl font-extrabold text-white mb-6" style={{ fontFamily: "Sora, sans-serif" }}>
              {profile?.name || "개발자"}
              <br />
              <span className="gradient-text">소개</span>
            </h2>
            <p className="text-slate-400 leading-relaxed mb-6 whitespace-pre-line">
              {profile?.introduction || "성장을 즐기는 개발자입니다. 관리자 페이지에서 소개를 작성해 보세요."}
            </p>
            {profile?.currentStatus && (
              <p className="text-slate-300 leading-relaxed mb-6">
                <span className="text-blue-400 font-semibold">현재</span> — {profile.currentStatus}
              </p>
            )}

            {/* 기본 정보 요약 (학력·병역·거주지·생년) */}
            {basicFacts.length > 0 && (
              <div className="grid grid-cols-2 gap-y-2 gap-x-4 mb-8 p-4 rounded-xl bg-white/3 border border-white/5">
                {basicFacts.map(({ icon: Icon, label, value }) => (
                  <div key={label} className="flex items-center gap-2 text-sm">
                    <Icon className="w-4 h-4 text-blue-400 shrink-0" />
                    <span className="text-xs text-slate-500 shrink-0">{label}</span>
                    <span className="text-slate-300 truncate">{value}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Highlight cards */}
            <div className="grid sm:grid-cols-2 gap-3">
              {highlights.map((item) => (
                <div key={item.title} className="glass-card p-4 rounded-xl hover:border-blue-500/30 transition-all duration-200">
                  <div className="text-2xl mb-2">{item.icon}</div>
                  <div className="text-sm font-semibold text-white mb-1">{item.title}</div>
                  <div className="text-xs text-slate-500 leading-relaxed">{item.desc}</div>
                </div>
              ))}
            </div>

            {/* 관심 & 성장 분야 */}
            <div className="mt-6">
              <div className="text-xs font-semibold text-slate-500 mb-2">관심 · 성장 중인 분야</div>
              <div className="flex flex-wrap gap-2">
                {interests.map((t) => (
                  <span key={t} className="text-xs px-3 py-1 rounded-full bg-blue-500/10 text-blue-300 border border-blue-500/20">
                    {t}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
