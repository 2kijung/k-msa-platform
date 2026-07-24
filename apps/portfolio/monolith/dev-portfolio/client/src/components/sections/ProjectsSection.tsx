/* ==========================================================
   프로젝트(Projects) 섹션 — DB(/api/projects)에서 받아와 표시
   각 카드: 제목 · 설명 · 상태 · "핵심 기술"(사용 스택 태그) · 링크
   ========================================================== */

import { useInView } from "@/hooks/useInView";
import { useEffect, useState } from "react";
import { projectApi, type ProjectData } from "@/lib/api";
import { ExternalLink, Github, FolderGit2 } from "lucide-react";

const statusMap: Record<string, { label: string; cls: string }> = {
  DEVELOPMENT: { label: "진행중", cls: "text-yellow-400 bg-yellow-400/10 border-yellow-400/25" },
  PRODUCTION: { label: "완료", cls: "text-green-400 bg-green-400/10 border-green-400/25" },
  ARCHIVED: { label: "보관", cls: "text-slate-400 bg-slate-400/10 border-slate-400/25" },
};

export default function ProjectsSection() {
  const { ref, inView } = useInView();
  const [projects, setProjects] = useState<ProjectData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    projectApi.getAll().then((res) => {
      const d: any = res.data;
      const list = Array.isArray(d) ? d : (d?.content ?? []);
      if (res.success) setProjects(list);
      setLoading(false);
    });
  }, []);

  if (!loading && projects.length === 0) return null;

  return (
    <section id="projects" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`text-center mb-16 transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <p className="section-label mb-3">Projects</p>
          <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
            주요 <span className="gradient-text">프로젝트</span>
          </h2>
          <p className="text-slate-500 mt-4 max-w-xl mx-auto">
            SI 프로젝트와 개인 프로젝트에서 수행한 개발 경험입니다.
          </p>
        </div>

        {loading ? (
          <p className="text-center text-slate-500">불러오는 중...</p>
        ) : (
          <div className="grid md:grid-cols-2 gap-6">
            {projects.map((project, idx) => {
              const st = statusMap[project.status] || statusMap.PRODUCTION;
              const techs = (project.technologies || "")
                .split(",")
                .map((t) => t.trim())
                .filter(Boolean);
              return (
                <div
                  key={project.id}
                  className={`glass-card rounded-2xl p-6 hover:border-blue-500/30 transition-all duration-500 group flex flex-col ${
                    inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
                  }`}
                  style={{ transitionDelay: `${idx * 80}ms` }}
                >
                  {/* header */}
                  <div className="flex items-start justify-between mb-3 gap-2">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-11 h-11 shrink-0 rounded-xl flex items-center justify-center bg-blue-500/10 border border-blue-500/25 text-blue-400">
                        <FolderGit2 className="w-5 h-5" />
                      </div>
                      <h3 className="text-sm font-bold text-white group-hover:text-blue-300 transition-colors leading-snug">
                        {project.title}
                      </h3>
                    </div>
                    <span className={`shrink-0 text-xs font-mono px-2.5 py-1 rounded-full border ${st.cls}`}>
                      {st.label}
                    </span>
                  </div>

                  {/* description */}
                  <p className="text-sm text-slate-400 leading-relaxed mb-5 flex-1 whitespace-pre-line">
                    {project.description}
                  </p>

                  {/* 핵심 기술 (사용 스택) */}
                  {techs.length > 0 && (
                    <div className="mb-4">
                      <div className="text-xs font-semibold text-slate-500 mb-2">핵심 기술 / 사용 스택</div>
                      <div className="flex flex-wrap gap-1.5">
                        {techs.map((t) => (
                          <span
                            key={t}
                            className="text-xs px-2.5 py-1 rounded-md bg-blue-500/10 text-blue-300 border border-blue-500/20"
                          >
                            {t}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* links */}
                  {(project.githubUrl || project.liveUrl) && (
                    <div className="flex items-center gap-4 pt-4 border-t border-white/5">
                      {project.githubUrl && (
                        <a href={project.githubUrl} target="_blank" rel="noopener noreferrer"
                          className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-white transition-colors">
                          <Github className="w-3.5 h-3.5" /> Source
                        </a>
                      )}
                      {project.liveUrl && (
                        <a href={project.liveUrl} target="_blank" rel="noopener noreferrer"
                          className="flex items-center gap-1.5 text-xs text-blue-400 hover:text-blue-300 transition-colors">
                          <ExternalLink className="w-3.5 h-3.5" /> Live
                        </a>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
