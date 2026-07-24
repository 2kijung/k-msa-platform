/* ==========================================================
   기본정보(Basic Info) 섹션
   백엔드 /api/profile 에서 DB 데이터를 받아와 표시
   ========================================================== */

import { useEffect, useState } from "react";
import { useInView } from "@/hooks/useInView";
import { profileApi, type ProfileData } from "@/lib/api";
import { User, GraduationCap, Shield, MapPin, Mail, Github, Cake, BookOpen } from "lucide-react";

export default function BasicInfoSection() {
  const { ref, inView } = useInView();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const load = async () => {
      const res = await profileApi.get();
      if (res.success && res.data) {
        setProfile(res.data);
      } else {
        setError(true);
      }
      setLoading(false);
    };
    load();
  }, []);

  // 표시할 항목들 (값이 있는 것만 렌더)
  const items = profile
    ? [
        { icon: User, label: "이름", value: profile.name },
        { icon: Cake, label: "생년", value: profile.birthDate },
        { icon: MapPin, label: "거주지", value: profile.location },
        { icon: GraduationCap, label: "대학교", value: profile.university },
        { icon: BookOpen, label: "전공", value: profile.major },
        { icon: GraduationCap, label: "졸업여부", value: profile.graduationStatus },
        { icon: Shield, label: "병역", value: profile.militaryStatus },
        { icon: Mail, label: "이메일", value: profile.email },
      ].filter((it) => it.value && it.value.trim() !== "")
    : [];

  return (
    <section id="basic-info" className="py-24 relative" style={{ background: "#060b18" }}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`transition-all duration-700 ${
            inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          {/* 헤더 */}
          <div className="text-center mb-12">
            {profile?.imageUrl && (
              <img
                src={profile.imageUrl}
                alt="프로필"
                className="w-28 h-28 rounded-full object-cover mx-auto mb-5 neon-border"
              />
            )}
            <p className="section-label mb-3">Basic Info</p>
            <h2
              className="text-4xl font-extrabold text-white"
              style={{ fontFamily: "Sora, sans-serif" }}
            >
              기본 정보
            </h2>
          </div>

          {/* 로딩 / 에러 상태 */}
          {loading && (
            <p className="text-center text-slate-500">불러오는 중...</p>
          )}
          {error && (
            <p className="text-center text-red-400">
              백엔드(/api/profile) 연결 실패 — 백엔드가 실행 중인지 확인하세요.
            </p>
          )}

          {/* 한줄 소개 */}
          {profile?.introduction && (
            <p className="text-center text-lg text-slate-300 mb-6 max-w-2xl mx-auto leading-relaxed">
              “{profile.introduction}”
            </p>
          )}

          {/* 현재 준비/진행 중 */}
          {profile?.currentStatus && (
            <div className="text-center mb-10">
              <span className="inline-flex items-center gap-2 glass-card px-4 py-2 rounded-full text-sm text-slate-300">
                <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
                현재: {profile.currentStatus}
              </span>
            </div>
          )}

          {/* 정보 카드 그리드 */}
          {profile && (
            <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 max-w-5xl mx-auto">
              {items.map(({ icon: Icon, label, value }) => (
                <div key={label} className="glass-card p-5 rounded-xl">
                  <Icon className="w-5 h-5 text-blue-400 mb-3" />
                  <div className="text-xs text-slate-500 mb-1">{label}</div>
                  <div className="text-base font-semibold text-white">{value}</div>
                </div>
              ))}
            </div>
          )}

          {/* 링크 (GitHub / Blog) */}
          {profile && (profile.githubUrl || profile.blogUrl) && (
            <div className="flex justify-center gap-4 mt-8">
              {profile.githubUrl && (
                <a
                  href={profile.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="glass-card px-5 py-3 rounded-xl flex items-center gap-2 text-slate-300 hover:text-white transition-colors"
                >
                  <Github className="w-4 h-4" /> GitHub
                </a>
              )}
              {profile.blogUrl && (
                <a
                  href={profile.blogUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="glass-card px-5 py-3 rounded-xl flex items-center gap-2 text-slate-300 hover:text-white transition-colors"
                >
                  <BookOpen className="w-4 h-4" /> Blog
                </a>
              )}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
