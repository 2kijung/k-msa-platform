/* ==========================================================
   기술 스택(Skills) 섹션 — DB(/api/skills) 기반, 카테고리별 칩(태그) 형태
   ========================================================== */

import { useInView } from "@/hooks/useInView";
import { useEffect, useState } from "react";
import { skillApi, type SkillData } from "@/lib/api";

export default function SkillsSection() {
  const { ref, inView } = useInView();
  const [skills, setSkills] = useState<SkillData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    skillApi.getAll().then((res) => {
      if (res.success && Array.isArray(res.data)) setSkills(res.data);
      setLoading(false);
    });
  }, []);

  if (!loading && skills.length === 0) return null;

  // 카테고리별 그룹핑 (등장 순서 유지)
  const grouped: { category: string; items: SkillData[] }[] = [];
  for (const s of skills) {
    let g = grouped.find((x) => x.category === s.category);
    if (!g) {
      g = { category: s.category, items: [] };
      grouped.push(g);
    }
    g.items.push(s);
  }

  return (
    <section
      id="skills"
      className="py-24 relative"
      style={{ background: "linear-gradient(180deg, #060b18 0%, #0a1628 50%, #060b18 100%)" }}
    >
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`text-center mb-14 transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <p className="section-label mb-3">Technical Skills</p>
          <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
            기술 <span className="gradient-text">스택</span>
          </h2>
        </div>

        {loading ? (
          <p className="text-center text-slate-500">불러오는 중...</p>
        ) : (
          <div className="space-y-6">
            {grouped.map((g, gi) => (
              <div
                key={g.category}
                className={`glass-card p-6 rounded-2xl transition-all duration-700 ${
                  inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
                }`}
                style={{ transitionDelay: `${gi * 100}ms` }}
              >
                <h3 className="text-sm font-bold text-blue-400 mb-4 tracking-wide uppercase">{g.category}</h3>
                <div className="flex flex-wrap gap-2.5">
                  {g.items.map((s) => (
                    <span
                      key={s.id}
                      title={s.description || undefined}
                      className={`inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-slate-200 border transition-all duration-200 hover:-translate-y-0.5 ${s.description ? "cursor-help" : ""}`}
                      style={{
                        background: `${s.color || "#3b82f6"}14`,
                        borderColor: `${s.color || "#3b82f6"}40`,
                      }}
                    >
                      <span className="w-2 h-2 rounded-full" style={{ background: s.color || "#3b82f6" }} />
                      {s.name}
                      {s.description && <span className="text-xs opacity-50">?</span>}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
