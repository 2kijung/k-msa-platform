/* ==========================================================
   개발 노트(Dev Notes) 섹션
   주요 케이스 3개 기본 노출 → 나머지 접기/펼치기
   ========================================================== */

import { useEffect, useState } from "react";
import { useInView } from "@/hooks/useInView";
import { devNoteApi, type DevNoteData } from "@/lib/api";
import { Lightbulb, AlertTriangle, CheckCircle2, ChevronDown, ChevronUp, Star } from "lucide-react";

// displayOrder 1~3: 면접에 들이댈 실무 핵심 케이스
const FEATURED_COUNT = 3;

function CodeBlock({ label, code, tone }: { label: string; code: string; tone: "before" | "after" }) {
  return (
    <div>
      <div className={`text-xs font-mono mb-1 ${tone === "before" ? "text-red-400" : "text-green-400"}`}>{label}</div>
      <pre className={`text-xs bg-black/40 border ${tone === "before" ? "border-red-500/20" : "border-green-500/20"} rounded-lg p-3 overflow-x-auto`}>
        <code className="text-slate-300">{code}</code>
      </pre>
    </div>
  );
}

function NoteCard({ n, featured }: { n: DevNoteData; featured?: boolean }) {
  const [open, setOpen] = useState(false);

  return (
    <div className={`glass-card rounded-xl overflow-hidden transition-all duration-200 ${featured ? "border-blue-500/20" : ""}`}>
      {/* 카드 헤더 — 항상 보임 */}
      <button
        className="w-full text-left p-5 flex items-start justify-between gap-3 hover:bg-white/2 transition-colors"
        onClick={() => setOpen(!open)}
      >
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1.5 flex-wrap">
            {featured && (
              <span className="flex items-center gap-1 text-xs bg-amber-500/15 text-amber-300 px-2 py-0.5 rounded font-mono">
                <Star className="w-3 h-3" /> 핵심
              </span>
            )}
            {n.category && (
              <span className="text-xs bg-blue-500/15 text-blue-300 px-2 py-0.5 rounded font-mono">
                {n.category}
              </span>
            )}
          </div>
          <h3 className="text-sm font-bold text-white leading-snug">{n.title}</h3>
          {!open && n.situation && (
            <p className="text-xs text-slate-500 mt-1 line-clamp-1">{n.situation.split('.')[0]}</p>
          )}
        </div>
        <span className="text-slate-500 shrink-0 mt-0.5">
          {open ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
        </span>
      </button>

      {/* 상세 내용 — 펼쳤을 때만 */}
      {open && (
        <div className="px-5 pb-5 border-t border-white/5 pt-4 space-y-4">
          {n.situation && (
            <div>
              <div className="flex items-center gap-1.5 text-sm font-semibold text-amber-400 mb-1">
                <AlertTriangle className="w-4 h-4" /> 상황 / 문제
              </div>
              <p className="text-sm text-slate-400 leading-relaxed whitespace-pre-line">{n.situation}</p>
            </div>
          )}
          {(n.codeBefore || n.codeAfter) && (
            <div className="grid md:grid-cols-2 gap-3">
              {n.codeBefore && <CodeBlock label="// Before (문제)" code={n.codeBefore} tone="before" />}
              {n.codeAfter && <CodeBlock label="// After (해결)" code={n.codeAfter} tone="after" />}
            </div>
          )}
          {n.solution && (
            <div>
              <div className="flex items-center gap-1.5 text-sm font-semibold text-green-400 mb-1">
                <CheckCircle2 className="w-4 h-4" /> 원인 & 해결
              </div>
              <p className="text-sm text-slate-400 leading-relaxed whitespace-pre-line">{n.solution}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function DevNotesSection() {
  const { ref, inView } = useInView();
  const [notes, setNotes] = useState<DevNoteData[]>([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(false);

  useEffect(() => {
    const load = async () => {
      const res = await devNoteApi.getAll();
      if (res.success && Array.isArray(res.data)) setNotes(res.data);
      setLoading(false);
    };
    load();
  }, []);

  if (!loading && notes.length === 0) return null;

  const featured = notes.slice(0, FEATURED_COUNT);
  const rest = notes.slice(FEATURED_COUNT);

  return (
    <section id="dev-notes" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`transition-all duration-700 ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}
        >
          <div className="text-center mb-12">
            <p className="section-label mb-3">Trouble Shooting</p>
            <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
              문제 <span className="gradient-text">해결 기록</span>
            </h2>
            <p className="text-slate-500 mt-3 text-sm">실무에서 마주친 장애·이슈와 해결 과정, 그리고 설계 결정</p>
          </div>

          {loading ? (
            <p className="text-center text-slate-500">불러오는 중...</p>
          ) : (
            <div className="space-y-3">
              {/* 핵심 케이스 — 항상 표시 */}
              {featured.map((n) => (
                <NoteCard key={n.id} n={n} featured />
              ))}

              {/* 나머지 — 접기/펼치기 */}
              {rest.length > 0 && (
                <>
                  {expanded && (
                    <div className="space-y-3 pt-1">
                      {rest.map((n) => (
                        <NoteCard key={n.id} n={n} />
                      ))}
                    </div>
                  )}
                  <div className="text-center pt-2">
                    <button
                      onClick={() => setExpanded(!expanded)}
                      className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full border border-slate-700/50 bg-slate-800/30 text-slate-400 hover:border-blue-500/40 hover:text-blue-300 text-sm font-medium transition-all"
                    >
                      {expanded ? (
                        <><ChevronUp className="w-4 h-4" /> 접기</>
                      ) : (
                        <><ChevronDown className="w-4 h-4" /> {rest.length}개 더 보기</>
                      )}
                    </button>
                  </div>
                </>
              )}
            </div>
          )}

          <div className="text-center mt-8 text-xs text-slate-600 flex items-center justify-center gap-1.5">
            <Lightbulb className="w-3.5 h-3.5" /> 실제 개발 중 마주친 문제와 해결 과정을 기록했습니다.
          </div>
        </div>
      </div>
    </section>
  );
}
