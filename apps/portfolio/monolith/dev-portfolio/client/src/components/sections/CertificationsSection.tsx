/* ==========================================================
   자격증(Certifications) 섹션
   백엔드 /api/certifications 에서 목록을 받아와 표시
   ========================================================== */

import { useEffect, useState } from "react";
import { useInView } from "@/hooks/useInView";
import { certificationApi, type CertificationData } from "@/lib/api";
import { Award } from "lucide-react";

export default function CertificationsSection() {
  const { ref, inView } = useInView();
  const [certs, setCerts] = useState<CertificationData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      const res = await certificationApi.getAll();
      if (res.success && Array.isArray(res.data)) setCerts(res.data);
      setLoading(false);
    };
    load();
  }, []);

  // 자격증이 하나도 없으면 섹션 자체를 숨김
  if (!loading && certs.length === 0) return null;

  return (
    <section id="certifications" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <div className="text-center mb-12">
            <p className="section-label mb-3">Certifications</p>
            <h2
              className="text-4xl font-extrabold text-white"
              style={{ fontFamily: "Sora, sans-serif" }}
            >
              자격증 <span className="gradient-text">&amp; 인증</span>
            </h2>
          </div>

          {loading ? (
            <p className="text-center text-slate-500">불러오는 중...</p>
          ) : (
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 max-w-5xl mx-auto">
              {certs.map((c) => (
                <div key={c.id} className="glass-card p-6 rounded-xl flex items-start gap-4">
                  <div className="shrink-0 w-11 h-11 rounded-lg bg-blue-500/10 flex items-center justify-center">
                    <Award className="w-6 h-6 text-blue-400" />
                  </div>
                  <div>
                    <div className="text-base font-semibold text-white">{c.name}</div>
                    {c.issuer && <div className="text-sm text-slate-400 mt-1">{c.issuer}</div>}
                    <div className="flex gap-2 mt-2 text-xs text-slate-500">
                      {c.acquiredDate && <span>{c.acquiredDate}</span>}
                      {c.score && <span className="text-blue-400">· {c.score}</span>}
                    </div>
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
