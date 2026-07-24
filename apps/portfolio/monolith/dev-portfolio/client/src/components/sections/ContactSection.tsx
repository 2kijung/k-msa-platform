/* ==========================================================
   Contact 섹션 — 연락처는 Profile(DB)에서, 문의 폼은 실제 /api/contacts 전송
   ========================================================== */

import { useInView } from "@/hooks/useInView";
import { useEffect, useState } from "react";
import { Mail, Github, BookOpen, MapPin, Send, CheckCircle } from "lucide-react";
import { profileApi, contactApi, type ProfileData } from "@/lib/api";
import { toast } from "sonner";

export default function ContactSection() {
  const { ref, inView } = useInView();
  const [form, setForm] = useState({ name: "", email: "", subject: "", message: "" });
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState<ProfileData | null>(null);

  useEffect(() => {
    profileApi.get().then((res) => {
      if (res.success && res.data) setProfile(res.data);
    });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const res = await contactApi.submit(form);
    setLoading(false);
    if (res.success) {
      setSubmitted(true);
    } else {
      toast.error("전송 실패: " + (res.error || res.message || "잠시 후 다시 시도해주세요"));
    }
  };

  // 값이 있는 것만 표시
  const contactInfo = [
    profile?.email && {
      icon: <Mail className="w-5 h-5" />, label: "Email",
      value: profile.email, href: `mailto:${profile.email}`, color: "#3b82f6",
    },
    profile?.githubUrl && {
      icon: <Github className="w-5 h-5" />, label: "GitHub",
      value: profile.githubUrl.replace(/^https?:\/\//, ""), href: profile.githubUrl, color: "#e2e8f0",
    },
    profile?.blogUrl && {
      icon: <BookOpen className="w-5 h-5" />, label: "Velog",
      value: profile.blogUrl.replace(/^https?:\/\//, ""), href: profile.blogUrl, color: "#20c997",
    },
    profile?.location && {
      icon: <MapPin className="w-5 h-5" />, label: "Location",
      value: profile.location, href: null, color: "#06b6d4",
    },
  ].filter(Boolean) as { icon: React.ReactNode; label: string; value: string; href: string | null; color: string }[];

  return (
    <section id="contact" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-[600px] h-[300px] bg-blue-600/5 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`text-center mb-16 transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <p className="section-label mb-3">Get In Touch</p>
          <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
            함께 일하고 <span className="gradient-text">싶으신가요?</span>
          </h2>
          <p className="text-slate-500 mt-4 max-w-xl mx-auto">
            새로운 기회, 협업, 또는 기술적 대화 모두 환영합니다.
          </p>
        </div>

        <div
          className={`grid lg:grid-cols-2 gap-12 transition-all duration-700 delay-200 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          {/* Left: Contact info */}
          <div>
            <h3 className="text-xl font-bold text-white mb-6" style={{ fontFamily: "Sora, sans-serif" }}>
              연락처 정보
            </h3>
            <div className="space-y-4 mb-8">
              {contactInfo.map((info) => (
                <div key={info.label} className="flex items-center gap-4">
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0"
                    style={{ background: `${info.color}15`, border: `1px solid ${info.color}30`, color: info.color }}
                  >
                    {info.icon}
                  </div>
                  <div>
                    <div className="text-xs text-slate-500 font-mono">{info.label}</div>
                    {info.href ? (
                      <a
                        href={info.href}
                        target={info.href.startsWith("http") ? "_blank" : undefined}
                        rel="noopener noreferrer"
                        className="text-sm text-slate-200 hover:text-blue-400 transition-colors animated-underline"
                      >
                        {info.value}
                      </a>
                    ) : (
                      <span className="text-sm text-slate-200">{info.value}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Availability card */}
            <div className="glass-card rounded-2xl p-6 border border-green-500/20">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-2.5 h-2.5 rounded-full bg-green-400 animate-pulse" />
                <span className="text-sm font-semibold text-green-400">연락 환영</span>
              </div>
              <p className="text-sm text-slate-400 leading-relaxed">
                Java/Spring 기반 백엔드 포지션 및 협업 제안에 관심이 있습니다. 편하게 연락 주세요.
              </p>
            </div>
          </div>

          {/* Right: Contact form */}
          <div className="glass-card rounded-2xl p-6">
            {submitted ? (
              <div className="flex flex-col items-center justify-center h-full py-12 text-center">
                <CheckCircle className="w-16 h-16 text-green-400 mb-4" />
                <h3 className="text-xl font-bold text-white mb-2">메시지 전송 완료!</h3>
                <p className="text-slate-400 text-sm">빠른 시일 내에 답변 드리겠습니다.</p>
                <button
                  onClick={() => { setSubmitted(false); setForm({ name: "", email: "", subject: "", message: "" }); }}
                  className="mt-6 px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition-colors"
                >
                  새 메시지 작성
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-mono text-slate-400 mb-1.5">이름 *</label>
                    <input
                      type="text" required value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                      placeholder="홍길동"
                      className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-slate-600 text-sm focus:outline-none focus:border-blue-500/50 transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-mono text-slate-400 mb-1.5">이메일 *</label>
                    <input
                      type="email" required value={form.email}
                      onChange={(e) => setForm({ ...form, email: e.target.value })}
                      placeholder="hello@example.com"
                      className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-slate-600 text-sm focus:outline-none focus:border-blue-500/50 transition-all"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-mono text-slate-400 mb-1.5">제목 *</label>
                  <input
                    type="text" required value={form.subject}
                    onChange={(e) => setForm({ ...form, subject: e.target.value })}
                    placeholder="프로젝트 협업 문의"
                    className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-slate-600 text-sm focus:outline-none focus:border-blue-500/50 transition-all"
                  />
                </div>
                <div>
                  <label className="block text-xs font-mono text-slate-400 mb-1.5">메시지 *</label>
                  <textarea
                    required rows={5} value={form.message}
                    onChange={(e) => setForm({ ...form, message: e.target.value })}
                    placeholder="안녕하세요! ..."
                    className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-slate-600 text-sm focus:outline-none focus:border-blue-500/50 transition-all resize-none"
                  />
                </div>
                <button
                  type="submit" disabled={loading}
                  className="w-full py-3.5 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:opacity-60 text-white font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 shadow-lg shadow-blue-500/20"
                >
                  {loading ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      전송 중...
                    </>
                  ) : (
                    <>
                      <Send className="w-4 h-4" /> 메시지 보내기
                    </>
                  )}
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
