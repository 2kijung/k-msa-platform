/* ==========================================================
   경력(Career) 섹션 — 회사 재직 이력 타임라인
   백엔드 /api/careers 에서 목록을 받아와 표시
   ========================================================== */

import { useEffect, useState } from "react";
import { useInView } from "@/hooks/useInView";
import { careerApi, type CareerData } from "@/lib/api";
import { Briefcase } from "lucide-react";

export default function CareerSection() {
  const { ref, inView } = useInView();
  const [careers, setCareers] = useState<CareerData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      const res = await careerApi.getAll();
      if (res.success && Array.isArray(res.data)) setCareers(res.data);
      setLoading(false);
    };
    load();
  }, []);

  if (!loading && careers.length === 0) return null;

  return (
    <section id="career" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <div className="text-center mb-12">
            <p className="section-label mb-3">Career</p>
            <h2
              className="text-4xl font-extrabold text-white"
              style={{ fontFamily: "Sora, sans-serif" }}
            >
              경력 <span className="gradient-text">사항</span>
            </h2>
          </div>

          {loading ? (
            <p className="text-center text-slate-500">불러오는 중...</p>
          ) : (
            <div className="relative border-l border-blue-500/20 ml-3 space-y-8">
              {careers.map((c) => (
                <div key={c.id} className="relative pl-8">
                  {/* 타임라인 점 */}
                  <div className="absolute -left-[9px] top-1 w-4 h-4 rounded-full bg-blue-500/20 border-2 border-blue-400 flex items-center justify-center">
                    <Briefcase className="w-2 h-2 text-blue-300" />
                  </div>
                  <div className="glass-card p-5 rounded-xl">
                    <div className="flex flex-wrap items-baseline justify-between gap-2">
                      <h3 className="text-lg font-bold text-white">{c.company}</h3>
                      <span className="text-xs font-mono text-blue-400">
                        {c.startDate} ~ {c.endDate ? c.endDate : "재직중"}
                      </span>
                    </div>
                    {c.position && <p className="text-sm text-slate-400 mt-1">{c.position}</p>}
                    {c.description && (
                      <p className="text-sm text-slate-500 mt-3 leading-relaxed whitespace-pre-line">
                        {c.description}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
