/* ==========================================================
   DESIGN: Midnight Architecture — Full-screen hero
   Particle background, typewriter text, tech badges, CTA buttons
   ========================================================== */

import ParticleBackground from "@/components/ParticleBackground";
import { useTypewriter } from "@/hooks/useTypewriter";
import { ChevronDown, Github, BookOpen, Mail, Download, FileText } from "lucide-react";
import { useEffect, useState } from "react";
import { profileApi, type ProfileData } from "@/lib/api";

const HERO_BG =
  "https://d2xsxph8kpxj0f.cloudfront.net/310519663398554856/hsrSa3YUTQvKpXz9DLCYxj/hero-bg-VUJYCrwNg7p49CEGA7jMJD.webp";

const roles = [
  "Backend Engineer",
  "Spring Boot Developer",
  "MSA Architect",
  "DevOps Engineer",
  "Kubernetes Expert",
];

const techBadges = [
  "JDK 17",
  "Spring Boot 3",
  "MSA",
  "Docker",
  "Kubernetes",
  "CI/CD",
];

export default function HeroSection() {
  const role = useTypewriter(roles, 80, 2200);
  const [profile, setProfile] = useState<ProfileData | null>(null);

  useEffect(() => {
    profileApi.get().then((res) => {
      if (res.success && res.data) setProfile(res.data);
    });
  }, []);

  const scrollToAbout = () => {
    document.getElementById("about")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <section
      id="hero"
      className="relative min-h-screen flex items-center justify-center overflow-hidden pt-24 pb-32"
      style={{ background: "#060b18" }}
    >
      {/* Background image with overlay */}
      <div
        className="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-20"
        style={{ backgroundImage: `url(${HERO_BG})` }}
      />
      {/* Deep gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-b from-[#060b18]/60 via-[#060b18]/40 to-[#060b18]" />
      {/* Grid pattern */}
      <div
        className="absolute inset-0 opacity-5"
        style={{
          backgroundImage:
            "linear-gradient(rgba(59,130,246,0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(59,130,246,0.5) 1px, transparent 1px)",
          backgroundSize: "60px 60px",
        }}
      />

      {/* Particle canvas */}
      <div className="absolute inset-0">
        <ParticleBackground />
      </div>

      {/* Glow orbs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-cyan-500/8 rounded-full blur-3xl pointer-events-none" />

      {/* Content */}
      <div className="relative z-10 text-center px-4 max-w-5xl mx-auto">
        {/* Status badge */}
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-green-500/10 border border-green-500/25 text-green-400 text-sm font-mono mb-8 animate-fadeIn">
          <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
          Available for new opportunities
        </div>

        {/* Main heading */}
        <h1
          className="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-white mb-4 animate-fadeInUp"
          style={{ fontFamily: "Sora, sans-serif", lineHeight: 1.1 }}
        >
          {profile?.name || "포트폴리오"}
        </h1>

        {/* Typewriter role */}
        <div className="h-12 flex items-center justify-center mb-6 animate-fadeInUp delay-200">
          <span className="text-xl sm:text-2xl font-mono text-slate-300">
            <span className="text-blue-400">{">"}</span>{" "}
            <span className="text-cyan-300">{role}</span>
            <span className="inline-block w-0.5 h-6 bg-cyan-400 ml-1 animate-[blink_1s_step-end_infinite]" />
          </span>
        </div>

        {/* Description */}
        <p className="text-slate-400 text-lg max-w-2xl mx-auto mb-8 leading-relaxed animate-fadeInUp delay-300">
          {profile?.introduction || "성장을 즐기는 개발자입니다."}
        </p>

        {/* Tech badges */}
        <div className="flex flex-wrap justify-center gap-2 mb-10 animate-fadeInUp delay-400">
          {techBadges.map((badge) => (
            <span key={badge} className="tech-badge">
              {badge}
            </span>
          ))}
        </div>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-8 animate-fadeInUp delay-500">
          <button
            onClick={() => document.getElementById("projects")?.scrollIntoView({ behavior: "smooth" })}
            className="px-8 py-3.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm transition-all duration-200 shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40 hover:-translate-y-0.5"
          >
            View Projects
          </button>
          <a
            href="/resume.pdf"
            download="이기정_자기소개서.pdf"
            className="px-8 py-3.5 rounded-xl glass-card text-slate-200 font-semibold text-sm transition-all duration-200 hover:-translate-y-0.5 flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            Download CV
          </a>
        </div>

        {/* Social links (프로필 데이터가 있는 것만 표시) */}
        <div className="flex items-center justify-center gap-4 animate-fadeInUp delay-600">
          {profile?.githubUrl && (
            <a
              href={profile.githubUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-10 h-10 rounded-lg glass-card flex items-center justify-center text-slate-400 hover:text-white transition-all duration-200 hover:-translate-y-0.5"
            >
              <Github className="w-5 h-5" />
            </a>
          )}
          {profile?.tistoryUrl && (
            <a
              href={profile.tistoryUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-10 h-10 rounded-lg glass-card flex items-center justify-center text-slate-400 hover:text-white transition-all duration-200 hover:-translate-y-0.5"
              title="Tistory"
            >
              {/* Tistory 로고 SVG */}
              <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
                <circle cx="12" cy="4.8" r="2.4"/>
                <circle cx="12" cy="12" r="2.4"/>
                <circle cx="12" cy="19.2" r="2.4"/>
              </svg>
            </a>
          )}
          {profile?.blogUrl && (
            <a
              href={profile.blogUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-10 h-10 rounded-lg glass-card flex items-center justify-center text-slate-400 hover:text-white transition-all duration-200 hover:-translate-y-0.5"
              title="Velog"
            >
              {/* Velog 로고 SVG */}
              <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
                <path d="M3 3h18v18H3V3zm3.75 5.25L10.5 16.5l3.75-8.25H18L12 19.5 6 8.25h.75z"/>
              </svg>
            </a>
          )}
          {profile?.email && (
            <a
              href={`mailto:${profile.email}`}
              className="w-10 h-10 rounded-lg glass-card flex items-center justify-center text-slate-400 hover:text-white transition-all duration-200 hover:-translate-y-0.5"
            >
              <Mail className="w-5 h-5" />
            </a>
          )}
        </div>
      </div>

      {/* Scroll indicator */}
      <button
        onClick={scrollToAbout}
        className="absolute bottom-6 left-1/2 -translate-x-1/2 hidden sm:flex flex-col items-center gap-2 text-slate-500 hover:text-slate-300 transition-colors animate-float"
      >
        <span className="text-xs font-mono tracking-widest">SCROLL</span>
        <ChevronDown className="w-5 h-5" />
      </button>
    </section>
  );
}
