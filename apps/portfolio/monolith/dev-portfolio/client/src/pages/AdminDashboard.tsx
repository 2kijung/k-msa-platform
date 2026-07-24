import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { contactApi, projectApi, visitorApi, authApi, profileApi, certificationApi, careerApi, fileApi, devNoteApi, skillApi, type ProfileData, type ProjectData, type CertificationData, type CareerData, type DevNoteData, type SkillData } from '@/lib/api';
import { Mail, Code, BarChart3, LogOut, User, Save, Award, Plus, Trash2, Briefcase, FileText, X, Layers } from 'lucide-react';
import { useLocation } from 'wouter';
import { toast } from 'sonner';

export default function AdminDashboard() {
  const [, setLocation] = useLocation();
  const [activeTab, setActiveTab] = useState('profile');
  const [contacts, setContacts] = useState<any[]>([]);
  const [projects, setProjects] = useState<any[]>([]);
  const [stats, setStats] = useState<any>(null);
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [savingProfile, setSavingProfile] = useState(false);
  const [certs, setCerts] = useState<CertificationData[]>([]);
  const [newCert, setNewCert] = useState<CertificationData>({ name: '', issuer: '', acquiredDate: '', score: '' });
  const [newProject, setNewProject] = useState<ProjectData>({ title: '', description: '', technologies: '', status: 'DEVELOPMENT' });
  const [careers, setCareers] = useState<CareerData[]>([]);
  const [newCareer, setNewCareer] = useState<CareerData>({ company: '', position: '', startDate: '', endDate: '', description: '' });
  const [uploadingImage, setUploadingImage] = useState(false);
  const [devNotes, setDevNotes] = useState<DevNoteData[]>([]);
  const emptyNote: DevNoteData = { title: '', category: '트러블슈팅', situation: '', codeBefore: '', codeAfter: '', solution: '' };
  const [noteForm, setNoteForm] = useState<DevNoteData>(emptyNote);
  const [skills, setSkills] = useState<SkillData[]>([]);
  const [newSkill, setNewSkill] = useState<SkillData>({ category: 'Backend', name: '', level: 80, color: '#3b82f6' });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [contactsRes, projectsRes, statsRes, profileRes, certsRes] = await Promise.all([
        contactApi.getAll(),
        projectApi.getAll(),
        visitorApi.getStats(),
        profileApi.get(),
        certificationApi.getAll(),
      ]);

      // 백엔드가 배열 또는 페이지객체({content:[...]})를 줄 수 있어 둘 다 처리
      const toArray = (d: any) => (Array.isArray(d) ? d : (d?.content ?? []));
      if (contactsRes.success) setContacts(toArray(contactsRes.data));
      if (projectsRes.success) setProjects(toArray(projectsRes.data));
      if (statsRes.success) setStats(statsRes.data);
      if (profileRes.success && profileRes.data) setProfile(profileRes.data);
      if (certsRes.success) setCerts(toArray(certsRes.data));
      const careersRes = await careerApi.getAll();
      if (careersRes.success) setCareers(toArray(careersRes.data));
      const notesRes = await devNoteApi.getAll();
      if (notesRes.success) setDevNotes(toArray(notesRes.data));
      const skillsRes = await skillApi.getAll();
      if (skillsRes.success) setSkills(toArray(skillsRes.data));
    } catch (error) {
      console.error('Failed to load data:', error);
    }
    setLoading(false);
  };

  const handleLogout = () => {
    authApi.logout();
    setLocation('/');
  };

  // 기본정보 폼 입력 변경
  const updateProfileField = (field: keyof ProfileData, value: string) => {
    setProfile((prev) => ({ ...(prev as ProfileData), [field]: value }));
  };

  // 기본정보 저장 (PUT /api/profile)
  const handleProfileSave = async () => {
    if (!profile) return;
    setSavingProfile(true);
    const res = await profileApi.update(profile);
    setSavingProfile(false);
    if (res.success) {
      toast.success('기본정보가 저장되었습니다. 홈 화면에 반영됩니다.');
      if (res.data) setProfile(res.data);
    } else {
      toast.error('저장 실패: ' + (res.error || res.message || '알 수 없는 오류'));
    }
  };

  // 자격증 추가
  const handleAddCert = async () => {
    if (!newCert.name.trim()) {
      toast.error('자격증명을 입력하세요.');
      return;
    }
    const res = await certificationApi.create(newCert);
    if (res.success) {
      toast.success('자격증이 추가되었습니다.');
      setNewCert({ name: '', issuer: '', acquiredDate: '', score: '' });
      loadData();
    } else {
      toast.error('추가 실패: ' + (res.error || res.message));
    }
  };

  // 자격증 삭제
  const handleDeleteCert = async (id?: number) => {
    if (!id) return;
    const res = await certificationApi.delete(id);
    if (res.success) {
      toast.success('삭제되었습니다.');
      loadData();
    } else {
      toast.error('삭제 실패: ' + (res.error || res.message));
    }
  };

  // 프로젝트 추가
  const handleAddProject = async () => {
    if (!newProject.title.trim()) {
      toast.error('프로젝트 제목을 입력하세요.');
      return;
    }
    const res = await projectApi.create(newProject);
    if (res.success) {
      toast.success('프로젝트가 추가되었습니다.');
      setNewProject({ title: '', description: '', technologies: '', status: 'DEVELOPMENT' });
      loadData();
    } else {
      toast.error('추가 실패: ' + (res.error || res.message));
    }
  };

  // 프로젝트 삭제
  const handleDeleteProject = async (id?: number) => {
    if (!id) return;
    const res = await projectApi.delete(id);
    if (res.success) {
      toast.success('삭제되었습니다.');
      loadData();
    } else {
      toast.error('삭제 실패: ' + (res.error || res.message));
    }
  };

  // 프로필 사진 업로드
  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !profile) return;
    setUploadingImage(true);
    const res = await fileApi.upload(file);
    setUploadingImage(false);
    if (res.success && res.data) {
      // 업로드된 이미지 URL을 프로필에 반영 (저장 버튼 눌러야 최종 저장됨)
      setProfile({ ...profile, imageUrl: res.data });
      toast.success('사진 업로드 완료. "저장"을 눌러 반영하세요.');
    } else {
      toast.error('업로드 실패: ' + (res.error || res.message));
    }
  };

  // 경력 추가
  const handleAddCareer = async () => {
    if (!newCareer.company.trim()) {
      toast.error('회사명을 입력하세요.');
      return;
    }
    const res = await careerApi.create(newCareer);
    if (res.success) {
      toast.success('경력이 추가되었습니다.');
      setNewCareer({ company: '', position: '', startDate: '', endDate: '', description: '' });
      loadData();
    } else {
      toast.error('추가 실패: ' + (res.error || res.message));
    }
  };

  // 경력 삭제
  const handleDeleteCareer = async (id?: number) => {
    if (!id) return;
    const res = await careerApi.delete(id);
    if (res.success) {
      toast.success('삭제되었습니다.');
      loadData();
    } else {
      toast.error('삭제 실패: ' + (res.error || res.message));
    }
  };

  // 개발노트: 폼 초기화 / 편집 로드
  const resetNoteForm = () => setNoteForm(emptyNote);
  const editNote = (n: DevNoteData) => setNoteForm({ ...n });

  // 개발노트 저장 (id 있으면 수정, 없으면 새로 작성)
  const handleSaveNote = async () => {
    if (!noteForm.title.trim()) {
      toast.error('제목을 입력하세요.');
      return;
    }
    const res = noteForm.id
      ? await devNoteApi.update(noteForm.id, noteForm)
      : await devNoteApi.create(noteForm);
    if (res.success) {
      toast.success(noteForm.id ? '수정되었습니다.' : '작성되었습니다.');
      resetNoteForm();
      loadData();
    } else {
      toast.error('저장 실패: ' + (res.error || res.message));
    }
  };

  const handleDeleteNote = async (id?: number) => {
    if (!id) return;
    const res = await devNoteApi.delete(id);
    if (res.success) {
      toast.success('삭제되었습니다.');
      if (noteForm.id === id) resetNoteForm();
      loadData();
    } else {
      toast.error('삭제 실패: ' + (res.error || res.message));
    }
  };

  // 스킬 추가
  const handleAddSkill = async () => {
    if (!newSkill.name.trim()) {
      toast.error('기술명을 입력하세요.');
      return;
    }
    const res = await skillApi.create(newSkill);
    if (res.success) {
      toast.success('기술이 추가되었습니다.');
      setNewSkill({ category: newSkill.category, name: '', level: 80, color: '#3b82f6' });
      loadData();
    } else {
      toast.error('추가 실패: ' + (res.error || res.message));
    }
  };

  const handleDeleteSkill = async (id?: number) => {
    if (!id) return;
    const res = await skillApi.delete(id);
    if (res.success) {
      toast.success('삭제되었습니다.');
      loadData();
    } else {
      toast.error('삭제 실패: ' + (res.error || res.message));
    }
  };

  // 기본정보 입력 필드 정의 (label, key)
  const profileFields: { label: string; key: keyof ProfileData }[] = [
    { label: '이름', key: 'name' },
    { label: '생년', key: 'birthDate' },
    { label: '거주지', key: 'location' },
    { label: '대학교', key: 'university' },
    { label: '전공', key: 'major' },
    { label: '졸업여부', key: 'graduationStatus' },
    { label: '병역', key: 'militaryStatus' },
    { label: '이메일', key: 'email' },
    { label: 'GitHub 주소', key: 'githubUrl' },
    { label: 'Tistory 주소 (주요 블로그)', key: 'tistoryUrl' },
    { label: 'Velog 주소', key: 'blogUrl' },
  ];

  return (
    <div className="min-h-screen bg-background text-foreground p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-4xl font-bold mb-2">Admin Dashboard</h1>
            <p className="text-muted-foreground">Manage your portfolio content</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setLocation('/')}>
              Home
            </Button>
            <Button variant="destructive" onClick={handleLogout}>
              <LogOut className="w-4 h-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>

        {/* Stats Cards */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Visitors</p>
                  <p className="text-3xl font-bold">{stats.totalVisitors}</p>
                </div>
                <BarChart3 className="w-8 h-8 text-primary opacity-50" />
              </div>
            </Card>
            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Unique IPs</p>
                  <p className="text-3xl font-bold">{stats.uniqueIPs}</p>
                </div>
                <BarChart3 className="w-8 h-8 text-primary opacity-50" />
              </div>
            </Card>
            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Sessions</p>
                  <p className="text-3xl font-bold">{stats.uniqueSessions}</p>
                </div>
                <BarChart3 className="w-8 h-8 text-primary opacity-50" />
              </div>
            </Card>
            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Page Views</p>
                  <p className="text-3xl font-bold">{stats.pageViews}</p>
                </div>
                <BarChart3 className="w-8 h-8 text-primary opacity-50" />
              </div>
            </Card>
          </div>
        )}

        {/* Content Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-8">
            <TabsTrigger value="profile" className="flex items-center gap-2">
              <User className="w-4 h-4" />
              기본정보
            </TabsTrigger>
            <TabsTrigger value="career" className="flex items-center gap-2">
              <Briefcase className="w-4 h-4" />
              경력
            </TabsTrigger>
            <TabsTrigger value="certifications" className="flex items-center gap-2">
              <Award className="w-4 h-4" />
              자격증
            </TabsTrigger>
            <TabsTrigger value="devnotes" className="flex items-center gap-2">
              <FileText className="w-4 h-4" />
              개발노트
            </TabsTrigger>
            <TabsTrigger value="skills" className="flex items-center gap-2">
              <Layers className="w-4 h-4" />
              스킬
            </TabsTrigger>
            <TabsTrigger value="contacts" className="flex items-center gap-2">
              <Mail className="w-4 h-4" />
              Contacts
            </TabsTrigger>
            <TabsTrigger value="projects" className="flex items-center gap-2">
              <Code className="w-4 h-4" />
              Projects
            </TabsTrigger>
            <TabsTrigger value="stats" className="flex items-center gap-2">
              <BarChart3 className="w-4 h-4" />
              Statistics
            </TabsTrigger>
          </TabsList>

          {/* 기본정보 Tab */}
          <TabsContent value="profile" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">기본정보 수정</h2>
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : !profile ? (
                <p className="text-muted-foreground">프로필을 불러오지 못했습니다.</p>
              ) : (
                <div className="space-y-4">
                  {/* 프로필 사진 */}
                  <div>
                    <label className="block text-sm text-muted-foreground mb-2">프로필 사진</label>
                    <div className="flex items-center gap-4">
                      {profile.imageUrl ? (
                        <img src={profile.imageUrl} alt="프로필" className="w-20 h-20 rounded-full object-cover border border-border" />
                      ) : (
                        <div className="w-20 h-20 rounded-full bg-muted flex items-center justify-center text-muted-foreground">
                          <User className="w-8 h-8" />
                        </div>
                      )}
                      <label className="cursor-pointer inline-flex items-center px-4 py-2 rounded-md border border-border hover:bg-muted text-sm">
                        {uploadingImage ? '업로드 중...' : '사진 선택'}
                        <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} disabled={uploadingImage} />
                      </label>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {profileFields.map(({ label, key }) => (
                      <div key={key}>
                        <label className="block text-sm text-muted-foreground mb-1">{label}</label>
                        <input
                          type="text"
                          value={(profile[key] as string) ?? ''}
                          onChange={(e) => updateProfileField(key, e.target.value)}
                          className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                        />
                      </div>
                    ))}
                  </div>
                  <div>
                    <label className="block text-sm text-muted-foreground mb-1">한줄 소개</label>
                    <textarea
                      value={profile.introduction ?? ''}
                      onChange={(e) => updateProfileField('introduction', e.target.value)}
                      rows={3}
                      className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-muted-foreground mb-1">현재 준비/진행 중인 것</label>
                    <textarea
                      value={profile.currentStatus ?? ''}
                      onChange={(e) => updateProfileField('currentStatus', e.target.value)}
                      rows={2}
                      placeholder="예: AWS 자격증과 쿠버네티스를 공부 중입니다."
                      className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <Button onClick={handleProfileSave} disabled={savingProfile}>
                    <Save className="w-4 h-4 mr-2" />
                    {savingProfile ? '저장 중...' : '저장'}
                  </Button>
                </div>
              )}
            </Card>
          </TabsContent>

          {/* 경력 Tab */}
          <TabsContent value="career" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">경력 관리</h2>

              {/* 추가 폼 */}
              <div className="border border-border rounded-lg p-4 mb-6">
                <p className="font-semibold mb-3">새 경력 추가</p>
                <div className="space-y-3 mb-3">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <input
                      type="text" placeholder="회사명"
                      value={newCareer.company}
                      onChange={(e) => setNewCareer({ ...newCareer, company: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <input
                      type="text" placeholder="직무 (예: 백엔드 개발자)"
                      value={newCareer.position ?? ''}
                      onChange={(e) => setNewCareer({ ...newCareer, position: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <input
                      type="text" placeholder="입사 (예: 2022-03)"
                      value={newCareer.startDate ?? ''}
                      onChange={(e) => setNewCareer({ ...newCareer, startDate: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <input
                      type="text" placeholder="퇴사 (비우면 재직중)"
                      value={newCareer.endDate ?? ''}
                      onChange={(e) => setNewCareer({ ...newCareer, endDate: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <textarea
                    placeholder="주요 업무/성과"
                    value={newCareer.description ?? ''}
                    onChange={(e) => setNewCareer({ ...newCareer, description: e.target.value })}
                    rows={2}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <Button onClick={handleAddCareer}>
                  <Plus className="w-4 h-4 mr-2" /> 추가
                </Button>
              </div>

              {/* 목록 */}
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : careers.length === 0 ? (
                <p className="text-muted-foreground">등록된 경력이 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {careers.map((c) => (
                    <div key={c.id} className="flex justify-between items-start border border-border rounded-lg p-4">
                      <div>
                        <p className="font-semibold">{c.company} <span className="text-sm text-muted-foreground">· {c.position}</span></p>
                        <p className="text-xs text-muted-foreground">{c.startDate} ~ {c.endDate || '재직중'}</p>
                        {c.description && <p className="text-sm text-muted-foreground mt-2">{c.description}</p>}
                      </div>
                      <Button variant="destructive" size="sm" onClick={() => handleDeleteCareer(c.id)}>
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* 자격증 Tab */}
          <TabsContent value="certifications" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">자격증 관리</h2>

              {/* 추가 폼 */}
              <div className="border border-border rounded-lg p-4 mb-6">
                <p className="font-semibold mb-3">새 자격증 추가</p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                  <input
                    type="text" placeholder="자격증명 (예: 정보처리기사)"
                    value={newCert.name}
                    onChange={(e) => setNewCert({ ...newCert, name: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    type="text" placeholder="발급기관 (예: 한국산업인력공단)"
                    value={newCert.issuer ?? ''}
                    onChange={(e) => setNewCert({ ...newCert, issuer: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    type="text" placeholder="취득일 (예: 2023-05)"
                    value={newCert.acquiredDate ?? ''}
                    onChange={(e) => setNewCert({ ...newCert, acquiredDate: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    type="text" placeholder="점수/등급 (선택)"
                    value={newCert.score ?? ''}
                    onChange={(e) => setNewCert({ ...newCert, score: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <Button onClick={handleAddCert}>
                  <Plus className="w-4 h-4 mr-2" /> 추가
                </Button>
              </div>

              {/* 목록 */}
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : certs.length === 0 ? (
                <p className="text-muted-foreground">등록된 자격증이 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {certs.map((c) => (
                    <div key={c.id} className="flex justify-between items-center border border-border rounded-lg p-4">
                      <div>
                        <p className="font-semibold">{c.name}</p>
                        <p className="text-sm text-muted-foreground">
                          {c.issuer} {c.acquiredDate && `· ${c.acquiredDate}`} {c.score && `· ${c.score}`}
                        </p>
                      </div>
                      <Button variant="destructive" size="sm" onClick={() => handleDeleteCert(c.id)}>
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* 개발노트 Tab */}
          <TabsContent value="devnotes" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">개발 노트 관리</h2>

              {/* 작성/수정 폼 */}
              <div className="border border-border rounded-lg p-4 mb-6">
                <div className="flex justify-between items-center mb-3">
                  <p className="font-semibold">{noteForm.id ? '노트 수정' : '새 노트 작성'}</p>
                  {noteForm.id && (
                    <Button variant="ghost" size="sm" onClick={resetNoteForm}>
                      <X className="w-4 h-4 mr-1" /> 새로 작성
                    </Button>
                  )}
                </div>
                <div className="space-y-3">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <input
                      type="text" placeholder="제목"
                      value={noteForm.title}
                      onChange={(e) => setNoteForm({ ...noteForm, title: e.target.value })}
                      className="md:col-span-2 px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <select
                      value={noteForm.category ?? '트러블슈팅'}
                      onChange={(e) => setNoteForm({ ...noteForm, category: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    >
                      <option value="트러블슈팅">트러블슈팅</option>
                      <option value="설계">설계</option>
                      <option value="기술선택">기술선택</option>
                      <option value="DB">DB</option>
                    </select>
                  </div>
                  <textarea
                    placeholder="상황 / 문제 (처음엔 이렇게 했더니 이런 오류가...)"
                    value={noteForm.situation ?? ''}
                    onChange={(e) => setNoteForm({ ...noteForm, situation: e.target.value })}
                    rows={2}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <textarea
                      placeholder="Before 코드 (문제가 된 코드, 선택)"
                      value={noteForm.codeBefore ?? ''}
                      onChange={(e) => setNoteForm({ ...noteForm, codeBefore: e.target.value })}
                      rows={5}
                      className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground font-mono text-xs focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <textarea
                      placeholder="After 코드 (수정한 코드, 선택)"
                      value={noteForm.codeAfter ?? ''}
                      onChange={(e) => setNoteForm({ ...noteForm, codeAfter: e.target.value })}
                      rows={5}
                      className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground font-mono text-xs focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <textarea
                    placeholder="원인 & 해결 / 교훈"
                    value={noteForm.solution ?? ''}
                    onChange={(e) => setNoteForm({ ...noteForm, solution: e.target.value })}
                    rows={3}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <Button onClick={handleSaveNote}>
                    <Save className="w-4 h-4 mr-2" /> {noteForm.id ? '수정 저장' : '작성'}
                  </Button>
                </div>
              </div>

              {/* 목록 */}
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : devNotes.length === 0 ? (
                <p className="text-muted-foreground">등록된 개발 노트가 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {devNotes.map((n) => (
                    <div key={n.id} className="flex justify-between items-start border border-border rounded-lg p-4">
                      <div>
                        <p className="font-semibold">
                          <span className="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded mr-2">{n.category}</span>
                          {n.title}
                        </p>
                        {n.situation && <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{n.situation}</p>}
                      </div>
                      <div className="flex gap-2 shrink-0">
                        <Button variant="outline" size="sm" onClick={() => editNote(n)}>수정</Button>
                        <Button variant="destructive" size="sm" onClick={() => handleDeleteNote(n.id)}>
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* 스킬 Tab */}
          <TabsContent value="skills" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">기술 스택 관리</h2>

              {/* 추가 폼 */}
              <div className="border border-border rounded-lg p-4 mb-6">
                <p className="font-semibold mb-3">새 기술 추가</p>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-3 items-center">
                  <input
                    type="text" placeholder="카테고리 (예: Backend)"
                    value={newSkill.category}
                    onChange={(e) => setNewSkill({ ...newSkill, category: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    type="text" placeholder="기술명 (예: Spring Boot)"
                    value={newSkill.name}
                    onChange={(e) => setNewSkill({ ...newSkill, name: e.target.value })}
                    className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-muted-foreground">숙련도</span>
                    <input
                      type="number" min={0} max={100}
                      value={newSkill.level ?? 80}
                      onChange={(e) => setNewSkill({ ...newSkill, level: Number(e.target.value) })}
                      className="w-20 px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <input
                    type="color"
                    value={newSkill.color ?? '#3b82f6'}
                    onChange={(e) => setNewSkill({ ...newSkill, color: e.target.value })}
                    className="h-10 w-16 rounded-md border border-border bg-background"
                  />
                </div>
                <Button onClick={handleAddSkill}>
                  <Plus className="w-4 h-4 mr-2" /> 추가
                </Button>
              </div>

              {/* 목록 */}
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : skills.length === 0 ? (
                <p className="text-muted-foreground">등록된 기술이 없습니다.</p>
              ) : (
                <div className="space-y-2">
                  {skills.map((s) => (
                    <div key={s.id} className="flex justify-between items-center border border-border rounded-lg p-3">
                      <div className="flex items-center gap-3">
                        <span className="w-3 h-3 rounded-full" style={{ background: s.color || '#3b82f6' }} />
                        <span className="text-xs bg-secondary/20 text-secondary-foreground px-2 py-0.5 rounded">{s.category}</span>
                        <span className="font-medium">{s.name}</span>
                        <span className="text-xs text-muted-foreground">{s.level}%</span>
                      </div>
                      <Button variant="destructive" size="sm" onClick={() => handleDeleteSkill(s.id)}>
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* Contacts Tab */}
          <TabsContent value="contacts" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">Contact Messages</h2>
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : contacts.length === 0 ? (
                <p className="text-muted-foreground">No contact messages yet</p>
              ) : (
                <div className="space-y-4">
                  {contacts.map((contact: any) => (
                    <div key={contact.id} className="border border-border rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <p className="font-semibold">{contact.name}</p>
                          <p className="text-sm text-muted-foreground">{contact.email}</p>
                        </div>
                        <span className="text-xs bg-primary/20 text-primary px-2 py-1 rounded">
                          {contact.status}
                        </span>
                      </div>
                      <p className="font-medium mb-2">{contact.subject}</p>
                      <p className="text-sm text-muted-foreground">{contact.message}</p>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* Projects Tab */}
          <TabsContent value="projects" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">프로젝트 관리</h2>

              {/* 추가 폼 */}
              <div className="border border-border rounded-lg p-4 mb-6">
                <p className="font-semibold mb-3">새 프로젝트 추가</p>
                <div className="space-y-3 mb-3">
                  <input
                    type="text" placeholder="프로젝트 제목"
                    value={newProject.title}
                    onChange={(e) => setNewProject({ ...newProject, title: e.target.value })}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <textarea
                    placeholder="설명"
                    value={newProject.description}
                    onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
                    rows={2}
                    className="w-full px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <input
                      type="text" placeholder="기술스택 (쉼표로 구분: Spring Boot, React)"
                      value={newProject.technologies}
                      onChange={(e) => setNewProject({ ...newProject, technologies: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    <select
                      value={newProject.status}
                      onChange={(e) => setNewProject({ ...newProject, status: e.target.value })}
                      className="px-3 py-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                    >
                      <option value="DEVELOPMENT">DEVELOPMENT</option>
                      <option value="PRODUCTION">PRODUCTION</option>
                      <option value="ARCHIVED">ARCHIVED</option>
                    </select>
                  </div>
                </div>
                <Button onClick={handleAddProject}>
                  <Plus className="w-4 h-4 mr-2" /> 추가
                </Button>
              </div>

              {/* 목록 */}
              {loading ? (
                <p className="text-muted-foreground">Loading...</p>
              ) : projects.length === 0 ? (
                <p className="text-muted-foreground">No projects yet</p>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {projects.map((project: any) => (
                    <div key={project.id} className="border border-border rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <h3 className="font-semibold">{project.title}</h3>
                        <Button variant="destructive" size="sm" onClick={() => handleDeleteProject(project.id)}>
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                      <p className="text-sm text-muted-foreground mb-3">{project.description}</p>
                      <div className="flex flex-wrap gap-2 mb-3">
                        {project.technologies?.split(',').map((tech: string, idx: number) => (
                          <span key={idx} className="text-xs bg-secondary/20 text-secondary-foreground px-2 py-1 rounded">
                            {tech.trim()}
                          </span>
                        ))}
                      </div>
                      <p className="text-xs text-muted-foreground">Status: {project.status}</p>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </TabsContent>

          {/* Statistics Tab */}
          <TabsContent value="stats" className="mt-6">
            <Card className="p-6">
              <h2 className="text-2xl font-bold mb-4">Detailed Statistics</h2>
              {stats ? (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="border border-border rounded-lg p-4">
                      <p className="text-sm text-muted-foreground mb-1">Total Visitors</p>
                      <p className="text-3xl font-bold">{stats.totalVisitors}</p>
                    </div>
                    <div className="border border-border rounded-lg p-4">
                      <p className="text-sm text-muted-foreground mb-1">Unique IPs</p>
                      <p className="text-3xl font-bold">{stats.uniqueIPs}</p>
                    </div>
                    <div className="border border-border rounded-lg p-4">
                      <p className="text-sm text-muted-foreground mb-1">Unique Sessions</p>
                      <p className="text-3xl font-bold">{stats.uniqueSessions}</p>
                    </div>
                    <div className="border border-border rounded-lg p-4">
                      <p className="text-sm text-muted-foreground mb-1">Page Views</p>
                      <p className="text-3xl font-bold">{stats.pageViews}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <p className="text-muted-foreground">No statistics available</p>
              )}
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
