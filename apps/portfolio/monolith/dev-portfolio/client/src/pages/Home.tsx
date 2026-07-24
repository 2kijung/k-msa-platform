/* ==========================================================
   DESIGN: Midnight Architecture — Main page composition
   Sections: Hero → About → Skills → Projects → Architecture → Contact
   ========================================================== */

import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import HeroSection from "@/components/sections/HeroSection";
import AboutSection from "@/components/sections/AboutSection";
import CareerSection from "@/components/sections/CareerSection";
import SkillsSection from "@/components/sections/SkillsSection";
import ProjectsSection from "@/components/sections/ProjectsSection";
import CertificationsSection from "@/components/sections/CertificationsSection";
import ArchitectureSection from "@/components/sections/ArchitectureSection";
import DevNotesSection from "@/components/sections/DevNotesSection";
import DevOpsSection from "@/components/sections/DevOpsSection";
import ContactSection from "@/components/sections/ContactSection";

export default function Home() {
  return (
    <div className="min-h-screen" style={{ background: "#060b18" }}>
      <Navbar />
      <main>
        <HeroSection />
        <AboutSection />
        <CareerSection />
        <SkillsSection />
        <ProjectsSection />
        <CertificationsSection />
        <ArchitectureSection />
        <DevOpsSection />
        <DevNotesSection />
        <ContactSection />
      </main>
      <Footer />
    </div>
  );
}
