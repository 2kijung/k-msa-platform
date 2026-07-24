/* ==========================================================
   DevOps 인프라 구축 기록 섹션
   전체 파이프라인: Docker → Jenkins → Kubernetes → 네트워크
   카드 클릭 → 모달로 상세 내용 확인
   ========================================================== */

import { useState } from "react";
import { useInView } from "@/hooks/useInView";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  Container, GitBranch, Server, Shield, Globe, AlertTriangle,
  CheckCircle2, ChevronRight, Layers, Network, Cpu, ChevronDown, ChevronUp, Github
} from "lucide-react";

type Category = "전체" | "Docker" | "Jenkins" | "Kubernetes" | "보안" | "네트워크";

interface DevOpsItem {
  id: number;
  category: Category;
  title: string;
  summary: string;
  icon: React.ReactNode;
  problem?: string;
  before?: string;
  after?: string;
  solution: string;
  tags: string[];
}

const ITEMS: DevOpsItem[] = [
  {
    id: 1,
    category: "Docker",
    title: "멀티스테이지 Docker 빌드",
    summary: "프론트(Node→Nginx)·백엔드(Maven→Corretto) 이미지 최적화",
    icon: <Container className="w-5 h-5" />,
    problem: "개발 의존성까지 포함된 이미지는 수백 MB — 배포 이미지는 런타임만 필요",
    before: "FROM node:22-alpine\nCOPY . .\nRUN pnpm install && pnpm build\n# 빌드 도구가 그대로 최종 이미지에 포함",
    after: "# Stage 1: 빌드\nFROM node:22-alpine AS builder\nRUN pnpm install && pnpm build\n\n# Stage 2: 서빙\nFROM nginx:1.27-alpine\nCOPY --from=builder /app/dist /usr/share/nginx/html",
    solution: "멀티스테이지 빌드로 최종 이미지에 빌드 도구 제거. 프론트엔드는 Nginx Alpine(~25MB), 백엔드는 Corretto Alpine으로 경량화. VITE_API_URL=/api 빌드 시 주입해 nginx가 /api/ 프록시를 처리하도록 설정.",
    tags: ["Docker", "Multi-stage", "Nginx", "Alpine"],
  },
  {
    id: 2,
    category: "Docker",
    title: "ARM64 이미지 호환성 오류",
    summary: "Apple Silicon(M-series)에서 eclipse-temurin 이미지 실행 불가",
    icon: <Cpu className="w-5 h-5" />,
    problem: "docker build 시 'no matching manifest for linux/arm64/v8' 에러 발생. Apple Silicon Mac에서 eclipse-temurin:17-jdk-alpine 이미지에 ARM64 지원이 없음.",
    before: "FROM eclipse-temurin:17-jdk-alpine\n# → linux/arm64 manifest 없음 → 빌드 실패",
    after: "FROM amazoncorretto:17-alpine3.21\n# → ARM64/AMD64 멀티아키텍처 지원",
    solution: "amazoncorretto:17-alpine3.21 이미지로 교체. Amazon Corretto는 ARM64(Apple Silicon) 및 AMD64 멀티아키텍처를 공식 지원. DOCKER_BUILDKIT=1 활성화 및 docker-buildx 설치로 크로스 플랫폼 빌드 환경 구성.",
    tags: ["ARM64", "Apple Silicon", "Corretto", "BuildKit"],
  },
  {
    id: 3,
    category: "Docker",
    title: "VITE_API_URL 하드코딩 → 외부 접속 실패",
    summary: "빌드된 프론트엔드가 외부에서 localhost:8080 호출",
    icon: <Container className="w-5 h-5" />,
    problem: "핸드폰·외부에서 포트폴리오 접속 후 로그인 시도 시 API 호출 실패. 브라우저가 사용자 자신의 PC의 localhost:8080으로 요청을 보내는 문제.",
    before: "// App.tsx\nexport const API_BASE_URL =\n  import.meta.env.VITE_API_URL\n  || 'http://localhost:8080/api'; // 하드코딩",
    after: "# Dockerfile 빌드 시 환경변수 주입\nRUN VITE_API_URL=/api pnpm run build\n# 상대경로 사용 → nginx가 /api/ 프록시 처리",
    solution: "VITE는 빌드 타임에 환경변수를 번들에 포함. VITE_API_URL=/api로 설정해 상대경로를 사용하면 nginx의 location /api/ 프록시 규칙이 백엔드로 트래픽을 전달. 도메인이 바뀌어도 동작.",
    tags: ["Vite", "환경변수", "nginx proxy", "CORS"],
  },
  {
    id: 4,
    category: "Jenkins",
    title: "호스트 네이티브 Jenkins 구성",
    summary: "Docker-in-Docker 소켓 문제 → brew install jenkins-lts로 전환",
    icon: <GitBranch className="w-5 h-5" />,
    problem: "Docker 컨테이너 안에서 Jenkins를 실행하면 Minikube/Docker 소켓에 접근이 안됨. Colima 소켓을 볼륨 마운트하는 방법도 보안 정책으로 차단. Jenkins 포트 8081은 IntelliJ 로컬 Spring Boot와 충돌.",
    before: "# docker-compose.yml (실패)\nservices:\n  jenkins:\n    image: jenkins/jenkins:lts\n    volumes:\n      - /var/run/docker.sock:/var/run/docker.sock\n    # → Colima 소켓 볼륨 마운트 불가",
    after: "# 호스트에서 직접 실행\nbrew install jenkins-lts\nnohup java -jar jenkins.war \\\n  --httpPort=9090 \\\n  -DJENKINS_HOME=~/.jenkins-portfolio &",
    solution: "호스트 네이티브 Jenkins로 전환. 호스트의 java, mvn, pnpm, kubectl, docker 명령어를 그대로 사용 가능. 포트를 9090으로 변경(8081 충돌 해결). Jenkinsfile.local 작성으로 Docker Hub 없이 로컬 Minikube에 직접 이미지 로드·배포 성공.",
    tags: ["Jenkins", "Pipeline", "brew", "Jenkinsfile"],
  },
  {
    id: 5,
    category: "Jenkins",
    title: "로컬 CI/CD 파이프라인 (Jenkinsfile.local)",
    summary: "Docker Hub 없이 Minikube에 직접 빌드→배포 자동화",
    icon: <GitBranch className="w-5 h-5" />,
    solution: "Jenkinsfile.local: Checkout → Build Backend JAR → Build Frontend → Docker Build(병렬) → minikube image load(병렬) → kubectl apply → Rollout 대기 → Health Check 7단계 파이프라인. 빌드 성공 시 ~67초 소요. minikube image load로 Docker Hub push 없이 클러스터에 이미지 주입.",
    tags: ["CI/CD", "Pipeline", "minikube image load", "Parallel"],
  },
  {
    id: 6,
    category: "Kubernetes",
    title: "K8s 전체 인프라 구성",
    summary: "Namespace~HPA까지 프로덕션 수준 K8s 매니페스트 작성",
    icon: <Layers className="w-5 h-5" />,
    solution: `구성 파일:\n• namespace.yaml — portfolio 네임스페이스 격리\n• configmap.yaml — DB 호스트/포트/이름 환경변수\n• secret.yaml — DB 인증정보·JWT Secret (base64)\n• postgres.yaml — StatefulSet + PVC(2Gi) + ClusterIP\n• backend.yaml — Deployment(2 replicas) + NodePort + HPA(2~5, CPU 70%)\n• frontend.yaml — Deployment(2 replicas) + NodePort\n• ingress.yaml — nginx ingress controller\n• network-policy.yaml — postgres는 backend만 접근 허용\n• pdb.yaml — minAvailable:1 (롤링 배포 중 가용성 보장)\n• resource-quota.yaml — pods:20, CPU:2, Memory:4Gi + LimitRange`,
    tags: ["K8s", "HPA", "StatefulSet", "NetworkPolicy", "PDB"],
  },
  {
    id: 7,
    category: "Kubernetes",
    title: "Spring Security → K8s Probe 403 오류",
    summary: "liveness/readiness probe가 /auth/health에서 403 반환",
    icon: <Server className="w-5 h-5" />,
    problem: "K8s가 백엔드 pod 상태 확인을 위해 GET /api/auth/health 호출 시 Spring Security가 401/403 반환 → pod가 Unhealthy 판정되어 재시작 무한 반복.",
    before: "# k8s/backend.yaml\nlivenessProbe:\n  httpGet:\n    path: /api/auth/health  # → 403 반환\n\n# SecurityConfig — 헬스체크 엔드포인트 없음",
    after: "// SecurityConfig.java\n.requestMatchers(\n  HttpMethod.GET, \"/auth/health\"\n).permitAll()  // probe 통과",
    solution: "Spring Security permitAll에 GET /auth/health 추가. K8s probe는 인증 헤더 없이 HTTP GET만 보내므로 해당 엔드포인트는 반드시 공개 접근 허용 필요.",
    tags: ["Spring Security", "K8s Probe", "liveness", "readiness"],
  },
  {
    id: 8,
    category: "Kubernetes",
    title: "imagePullPolicy 캐시 이슈",
    summary: "새 이미지 빌드해도 K8s가 이전 이미지 계속 사용",
    icon: <Server className="w-5 h-5" />,
    problem: "Jenkins 재빌드 후 minikube image load를 했는데도 kubectl rollout restart 후 이전 버전이 계속 실행됨.",
    before: "image: portfolio-backend:latest\nimagePullPolicy: IfNotPresent\n# latest 태그 + IfNotPresent\n# → 캐시 이미지 사용, 새 이미지 무시",
    after: "# Jenkinsfile.local에서 태그 동적 생성\nIMAGE_TAG = \"${BUILD_NUMBER}\"\nimage: portfolio-backend:${BUILD_NUMBER}\n# 태그가 바뀌면 새 이미지로 인식",
    solution: "latest 태그 + IfNotPresent 조합은 캐시를 사용. 빌드 번호를 태그로 사용하면 K8s가 새 이미지로 인식해 정상 교체. 로컬 개발 시 minikube image load 후 이미지 태그도 함께 변경 필요.",
    tags: ["imagePullPolicy", "minikube", "태그 전략"],
  },
  {
    id: 9,
    category: "Kubernetes",
    title: "Minikube Docker Desktop 메모리 한도",
    summary: "6144MB 요청 → Docker Desktop 5910MB 한도 초과 오류",
    icon: <Server className="w-5 h-5" />,
    problem: "Colima에서 Docker Desktop으로 전환 후 minikube start --memory=6144 실행 시 'Docker Desktop has only 5910MB memory but you specified 6144MB' 에러.",
    before: "minikube start --driver=docker --memory=6144\n# → MK_USAGE 에러",
    after: "minikube start --driver=docker \\\n  --cpus=4 --memory=5500\n# Docker Desktop 할당량 이내",
    solution: "Docker Desktop Resources 설정에서 최대 메모리 확인 후 그 이하로 설정. 5500MB로 조정하여 정상 기동. start-all.sh에도 반영.",
    tags: ["Minikube", "Docker Desktop", "메모리"],
  },
  {
    id: 10,
    category: "보안",
    title: "Actuator 전체 공개 → 정보 노출",
    summary: "/actuator/** permitAll → 메모리·빈·환경변수 외부 노출",
    icon: <Shield className="w-5 h-5" />,
    problem: "Spring Boot Actuator의 /actuator/env, /actuator/beans, /actuator/heapdump 등이 인증 없이 외부에서 접근 가능. DB 접속 정보, 환경변수, 클래스 목록 노출 위험.",
    before: "# SecurityConfig\n.requestMatchers(\"/actuator/**\").permitAll()\n\n# application-prod.yml\n# actuator 노출 설정 없음 → 전체 공개",
    after: "# application-prod.yml\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: health,info  # 최소 공개\n\n# SecurityConfig\n.requestMatchers(\n  \"/actuator/health\",\n  \"/actuator/info\"\n).permitAll()",
    solution: "application-prod.yml에서 노출 엔드포인트를 health/info만으로 제한하고, SecurityConfig도 동일하게 좁힘. K8s probe는 /actuator/health 대신 /auth/health를 사용하므로 영향 없음.",
    tags: ["Actuator", "보안", "정보노출", "Spring Security"],
  },
  {
    id: 11,
    category: "보안",
    title: "nginx Rate Limiting + 보안 헤더",
    summary: "로그인 Brute Force 방지, 보안 헤더 적용",
    icon: <Shield className="w-5 h-5" />,
    before: "# nginx.conf (변경 전)\n# Rate limiting 없음\n# 보안 헤더 없음\nlocation /api/ {\n    proxy_pass http://portfolio-backend:8080/api/;\n}",
    after: "limit_req_zone $binary_remote_addr\n  zone=login_limit:10m rate=5r/m;\n\nlocation /api/auth/login {\n    limit_req zone=login_limit burst=3;\n    # 분당 5회 제한\n}\n\nadd_header X-Frame-Options \"SAMEORIGIN\";\nadd_header X-Content-Type-Options \"nosniff\";",
    solution: "로그인 엔드포인트에 분당 5회 Rate Limit (burst=3), 일반 API는 초당 20회 제한. X-Frame-Options, X-Content-Type-Options, X-XSS-Protection 보안 헤더 추가로 클릭재킹·MIME 스니핑 방지.",
    tags: ["nginx", "Rate Limiting", "보안 헤더", "Brute Force"],
  },
  {
    id: 12,
    category: "네트워크",
    title: "DuckDNS + 공유기 포트포워딩",
    summary: "무료 서브도메인으로 외부 인터넷 접속 구성",
    icon: <Globe className="w-5 h-5" />,
    problem: "kubectl port-forward는 localhost에만 바인딩 → 같은 Wi-Fi 내에서도 다른 기기 접속 불가. 공인 IP는 변동 IP라 DNS 설정 필요.",
    solution: `구성 단계:\n1. duckdns.org 가입 → k-devops.duckdns.org 서브도메인 생성\n2. scripts/duckdns-update.sh → DuckDNS API로 공인IP 자동 갱신\n3. launchctl LaunchAgent 등록 → 5분마다 IP 업데이트\n4. KT 공유기: 장치설정 → 트래픽관리 → 포트포워딩\n   외부 80 → 172.30.1.94:80 (Caddy)\n   외부 443 → 172.30.1.94:443 (Caddy)\n5. port-forward --address 0.0.0.0 으로 바인딩 변경`,
    tags: ["DuckDNS", "포트포워딩", "DDNS", "KT"],
  },
  {
    id: 13,
    category: "네트워크",
    title: "Caddy 리버스 프록시 + Let's Encrypt",
    summary: "자동 HTTPS 인증서 발급, NAT Loopback 해결",
    icon: <Globe className="w-5 h-5" />,
    problem: "1) HTTP만 지원 시 보안 취약. 2) 공유기가 NAT Loopback 미지원 → Mac에서 자신의 공인 IP 도메인 접속 불가. 3) Chrome HSTS 캐시로 HTTP 도메인 접속 차단.",
    before: "# 기존: kubectl port-forward만 사용\n# → localhost에서만 접근 가능\n# → HTTPS 없음",
    after: "# /opt/homebrew/etc/Caddyfile\nhttp://k-devops.duckdns.org {\n    reverse_proxy localhost:8888\n}\n# Caddy가 Let's Encrypt 인증서 자동 발급\n# sudo brew services start caddy",
    solution: "Caddy를 Mac에서 리버스 프록시로 실행. sudo로 포트 80/443 바인딩. /etc/hosts에 127.0.0.1 k-devops.duckdns.org 추가로 NAT Loopback 우회. Chrome HSTS 캐시는 chrome://net-internals/#hsts에서 도메인 삭제로 해결. Let's Encrypt DuckDNS CAA 조회 지연은 자동 재시도로 해결.",
    tags: ["Caddy", "Let's Encrypt", "HTTPS", "NAT Loopback", "HSTS"],
  },
  {
    id: 14,
    category: "Docker",
    title: "Colima → Docker Desktop 마이그레이션",
    summary: "Docker Desktop 설치 실패(/usr/local 권한) 해결 과정",
    icon: <Container className="w-5 h-5" />,
    problem: "Docker Desktop 미설치 상태에서 Colima 사용 중. brew install --cask docker 실패: /usr/local/bin, /usr/local/cli-plugins 디렉토리 부재 or root 소유.",
    before: "brew install --cask docker\n# → /usr/local/cli-plugins 없음\n# → sudo mkdir 후 재시도\n# → chown: /usr/local: Operation not permitted (SIP)",
    after: "# SIP 때문에 /usr/local 자체는 chown 불가\n# 하위 디렉토리만 개별 chown\nsudo mkdir -p /usr/local/cli-plugins\nsudo chown $(whoami) /usr/local/cli-plugins\nbrew install --cask docker  # 성공",
    solution: "macOS SIP(System Integrity Protection)로 /usr/local 자체 소유권 변경 불가. /usr/local/bin, /usr/local/cli-plugins 등 필요한 하위 디렉토리만 개별 생성·소유권 변경. Docker Desktop 4.81.0 설치 성공 후 Minikube 드라이버를 Docker Desktop으로 교체.",
    tags: ["Docker Desktop", "SIP", "Colima", "brew cask"],
  },
  {
    id: 15,
    category: "Kubernetes",
    title: "사진 업로드 pod 재시작 시 소실",
    summary: "배포할 때마다 업로드한 사진이 사라지는 문제",
    icon: <Server className="w-5 h-5" />,
    problem: "새 버전 배포 시 K8s가 pod를 교체하면 컨테이너 내부 파일시스템이 초기화됨. ./uploads/에 저장된 사진이 모두 사라짐.",
    before: "# backend.yaml\n# volumeMount 없음\n# pod 교체 시 /app/uploads/ 전부 소실",
    after: "# k8s/uploads-pvc.yaml\napiVersion: v1\nkind: PersistentVolumeClaim\nmetadata:\n  name: uploads-pvc\nspec:\n  accessModes: [ReadWriteOnce]\n  resources:\n    requests:\n      storage: 2Gi\n\n# backend.yaml\nvolumeMounts:\n  - name: uploads-storage\n    mountPath: /app/uploads",
    solution: "PersistentVolumeClaim(PVC) 2Gi 생성 후 백엔드 deployment에 /app/uploads 경로로 마운트. pod가 교체되어도 PVC는 유지되므로 사진 영구 보존. ReadWriteOnce + 단일 노드(Minikube)라 두 pod 모두 같은 PVC 마운트 가능. Jenkinsfile.local에도 uploads-pvc.yaml 자동 적용 추가.",
    tags: ["PVC", "영구스토리지", "K8s", "파일업로드"],
  },
  {
    id: 16,
    category: "네트워크",
    title: "nginx 정규식이 API 이미지 경로 가로채기",
    summary: "/api/uploads/*.jpg가 정적파일 규칙에 걸려 404 반환",
    icon: <Globe className="w-5 h-5" />,
    problem: "사진을 업로드하고 PVC에 저장까지 됐는데 브라우저에서 /api/uploads/photo.jpg 요청 시 404 반환. 직접 백엔드(localhost:8889)로 접근하면 200인데 nginx 경유하면 404.",
    before: "# nginx.conf\nlocation ~* \\.(js|css|png|jpg|jpeg|gif|...)$ {\n    expires 1y;\n    # /api/uploads/photo.jpg 도 .jpg로 끝나서 여기 걸림\n    # → 프론트 정적폴더에서 찾다가 없으면 404\n}",
    after: "# nginx 정규식에서 /api/ 경로 제외\nlocation ~* ^(?!/api/).*\\.(js|css|png|jpg|jpeg|gif|...)$ {\n    expires 1y;\n    # /api/uploads/photo.jpg → 제외됨\n    # → /api/ 프록시 규칙이 처리\n}",
    solution: "nginx는 정규식(~) location이 prefix(/) location보다 우선순위가 높음. .jpg 확장자 매칭 규칙이 /api/uploads/*.jpg도 가로채서 프론트엔드 정적 폴더에서 파일을 찾으려다 없어 404 반환. 정규식 앞에 negative lookahead ^(?!/api/)를 추가해 /api/ 경로는 정적파일 규칙에서 제외.",
    tags: ["nginx", "정규식", "location 우선순위", "404"],
  },
  {
    id: 19,
    category: "Jenkins",
    title: "openclaw-msa Maven 멀티모듈 빌드 순서 제약",
    summary: "openclaw-common을 먼저 install 하지 않으면 3개 서비스가 전부 컴파일 실패",
    icon: <GitBranch className="w-5 h-5" />,
    problem: "mvn compile -pl budget-service,blog-service,notification-service 병렬 실행 시 'Could not resolve com.openclaw:openclaw-common:1.0.0-SNAPSHOT' 에러. 로컬 Maven 저장소에 openclaw-common이 없기 때문.",
    before: "// 잘못된 순서 — 병렬로 전부 빌드 시도\nmvn compile -pl budget-service,blog-service,notification-service\n// → openclaw-common 의존성 해결 실패\n// → 3개 서비스 모두 컴파일 에러",
    after: "// Jenkinsfile 파이프라인 순서\n// Stage 1: BOM 설치 (직렬 — 순서 강제)\nmvn install -N -q                       // 부모 POM\nmvn install -pl openclaw-common -q      // 공통 모듈\n\n// Stage 2: 서비스 빌드 (병렬 — common 설치 후)\nparallel budget-service, blog-service, notification-service",
    solution: "Maven 멀티모듈에서 공통 모듈(openclaw-common)은 로컬 저장소(~/.m2)에 install 되어야 다른 모듈이 의존성으로 참조 가능. Jenkinsfile에서 Stage를 분리해 BOM→common install을 먼저 직렬로 실행하고, 이후 3개 서비스를 parallel로 빌드. 순서 의존성이 있는 멀티모듈은 병렬화 전 선행 빌드 단계 분리가 필수.",
    tags: ["Maven", "Multi-module", "Jenkins", "parallel", "install"],
  },
  {
    id: 20,
    category: "Kubernetes",
    title: "MSA 단일 로그인 — JWT K8s Secret 공유",
    summary: "3개 서비스가 각자 로그인하면 비밀번호를 3번 입력해야 하는 문제",
    icon: <Server className="w-5 h-5" />,
    problem: "MSA에서 서비스마다 독립적인 인증을 구현하면 사용자는 budget/blog/notify 서비스마다 개별 로그인 필요. 또한 서비스 간 세션 공유가 불가능(MSA 특성상 서버 간 HttpSession 미공유).",
    before: "// 각 서비스가 독립적인 세션 인증\n// → budget-service 로그인 세션이 blog-service에서 유효하지 않음\n// → 서비스 전환마다 재로그인 필요\n// → 로드밸런서 세션 고정(sticky session) 필요 = 단점",
    after: "// budget-service /auth/login → JWT 발급\n// Nginx auth_request → budget-service /auth/verify\n// 검증 성공 시 X-User-Id 헤더 추가 후 하위 서비스로 전달\n\n// k8s/secret.yaml\nJWT_SECRET: <base64>  // 3개 서비스가 동일 Secret 참조",
    solution: "JWT를 K8s Secret에 저장해 3개 서비스가 동일한 서명 키를 공유. budget-service /auth/login에서만 토큰 발급. 이후 Nginx auth_request 모듈이 모든 요청을 /auth/verify에서 검증 → 유효하면 X-User-Id 헤더를 추가해 하위 서비스로 전달. 상태 비저장(Stateless) 방식이므로 서비스 확장 시 세션 공유 문제 없음.",
    tags: ["JWT", "K8s Secret", "Nginx auth_request", "Stateless", "MSA"],
  },
  {
    id: 21,
    category: "Jenkins",
    title: "MSA 서비스 간 Java import 컴파일 에러 → REST 분리 강제",
    summary: "budget-service에서 notification 클래스 직접 import → package not found",
    icon: <GitBranch className="w-5 h-5" />,
    problem: "MailParserJobConfig에서 TelegramNotificationService를 직접 import 하려다 'package com.openclaw.notification does not exist' 컴파일 에러 발생. MSA에서 서비스 간 Java 클래스 직접 참조는 Maven 모듈 경계를 위반.",
    before: "// budget-service/MailParserJobConfig.java\nimport com.openclaw.notification.service.TelegramNotificationService;\n// → notification-service가 별도 Maven 모듈\n// → budget-service pom.xml에 의존성 없음\n// → package com.openclaw.notification does not exist",
    after: "// budget-service/NotificationClient.java\n// notification-service를 직접 import 하지 않고\n// REST POST로만 통신\nrestTemplate.postForObject(\n    notifyUrl + \"/notify/send\",\n    payload, Void.class\n);",
    solution: "MSA 서비스 간 통신은 Java 클래스 참조가 아닌 REST API 호출만 허용. Maven 멀티모듈 구조가 이를 빌드 시점에 물리적으로 강제 — 다른 서비스 패키지를 import하면 컴파일 에러로 즉시 차단. budget-service에 NotificationClient(RestTemplate 래퍼)를 작성해 느슨한 결합 구현. 아키텍처 규칙이 코드 컨벤션이 아닌 빌드 시스템으로 보장됨.",
    tags: ["MSA", "Maven", "REST", "느슨한결합", "컴파일에러"],
  },
  {
    id: 17,
    category: "보안",
    title: "Prometheus + Grafana 모니터링",
    summary: "kube-prometheus-stack으로 K8s 클러스터 관측성 구성",
    icon: <Shield className="w-5 h-5" />,
    solution: "Helm으로 kube-prometheus-stack 배포. Prometheus가 K8s 메트릭(pod CPU/메모리, 노드 상태) 수집, Grafana 대시보드로 시각화. kubectl port-forward로 Grafana(3000), Prometheus(9091) 로컬 접근. 기본 계정: admin/grafana1234.",
    tags: ["Prometheus", "Grafana", "Helm", "Monitoring", "kube-prometheus-stack"],
  },
  {
    id: 18,
    category: "Kubernetes",
    title: "포트포워드 자동 재연결 데몬",
    summary: "Jenkins 배포 시 포트포워드 끊김 → macOS LaunchAgent로 자동 복구",
    icon: <Server className="w-5 h-5" />,
    problem: "Jenkins가 새 이미지를 K8s에 배포하면 pod가 교체되면서 kubectl port-forward 연결이 끊김. 수동으로 포트포워드를 재시작해야 사이트가 다시 접속됨.",
    before: "# Jenkins Summary 단계\nnohup kubectl port-forward ... &\ndisown $!\n# → Jenkins 종료 시 자식 프로세스 함께 종료\n# → 포트포워드 재시작 불안정",
    after: "# scripts/port-forward-daemon.sh\nforward_frontend() {\n  while true; do\n    kubectl port-forward ... svc/portfolio-frontend 8888:80\n    sleep 5  # 끊기면 5초 후 자동 재연결\n  done\n}\n# LaunchAgent KeepAlive: true → 항상 실행 유지",
    solution: "while true 루프로 포트포워드가 끊기면 5초 후 자동 재연결하는 데몬 스크립트 작성. macOS LaunchAgent(KeepAlive: true)로 등록해 Mac 부팅 시 자동 시작. Jenkins는 배포 후 localhost:8888 HTTP 200 응답을 최대 60초간 폴링해 서비스 복구를 검증 후 성공/실패 판정.",
    tags: ["LaunchAgent", "port-forward", "데몬", "자동재연결", "Jenkins"],
  },
];

const CATEGORY_ICONS: Record<Category, React.ReactNode> = {
  전체: <Layers className="w-4 h-4" />,
  Docker: <Container className="w-4 h-4" />,
  Jenkins: <GitBranch className="w-4 h-4" />,
  Kubernetes: <Server className="w-4 h-4" />,
  보안: <Shield className="w-4 h-4" />,
  네트워크: <Network className="w-4 h-4" />,
};

const CATEGORY_COLORS: Record<Category, string> = {
  전체: "text-slate-300 bg-slate-500/20",
  Docker: "text-blue-300 bg-blue-500/15",
  Jenkins: "text-orange-300 bg-orange-500/15",
  Kubernetes: "text-purple-300 bg-purple-500/15",
  보안: "text-green-300 bg-green-500/15",
  네트워크: "text-cyan-300 bg-cyan-500/15",
};

function CodeBlock({ label, code, tone }: { label: string; code: string; tone: "before" | "after" }) {
  return (
    <div>
      <div className={`text-xs font-mono mb-1 ${tone === "before" ? "text-red-400" : "text-green-400"}`}>{label}</div>
      <pre className={`text-xs bg-black/50 border ${tone === "before" ? "border-red-500/20" : "border-green-500/20"} rounded-lg p-3 overflow-x-auto whitespace-pre-wrap`}>
        <code className="text-slate-300">{code}</code>
      </pre>
    </div>
  );
}

export default function DevOpsSection() {
  const { ref, inView } = useInView();
  const [selected, setSelected] = useState<DevOpsItem | null>(null);
  const [activeCategory, setActiveCategory] = useState<Category>("전체");
  const [expanded, setExpanded] = useState(false);
  const INITIAL_COUNT = 6;

  const categories: Category[] = ["전체", "Docker", "Jenkins", "Kubernetes", "보안", "네트워크"];
  const filtered = activeCategory === "전체" ? ITEMS : ITEMS.filter(i => i.category === activeCategory);
  const visible = expanded ? filtered : filtered.slice(0, INITIAL_COUNT);
  const hasMore = filtered.length > INITIAL_COUNT;

  return (
    <section id="devops" className="py-24 relative" style={{ background: "#070d1e" }}>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div
          ref={ref}
          className={`transition-all duration-700 ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}
        >
          {/* 헤더 */}
          <div className="text-center mb-12">
            <p className="section-label mb-3">Infrastructure</p>
            <h2 className="text-4xl font-extrabold text-white" style={{ fontFamily: "Sora, sans-serif" }}>
              DevOps <span className="gradient-text">구축 기록</span>
            </h2>
            <p className="text-slate-500 mt-3 text-sm">
              Docker · Jenkins · Kubernetes · 네트워크 — 전체 파이프라인 구성 과정과 해결한 문제들
            </p>
            <div className="flex items-center justify-center gap-3 mt-5">
              <a
                href="https://github.com/2kijung/K-portfolio/blob/main/Jenkinsfile"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-slate-700/50 bg-slate-800/40 text-slate-400 hover:border-orange-500/40 hover:text-orange-300 text-xs font-mono transition-all"
              >
                <Github className="w-3.5 h-3.5" />
                Jenkinsfile (K-portfolio)
              </a>
              <a
                href="https://github.com/2kijung/openclaw-msa/blob/main/Jenkinsfile"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-slate-700/50 bg-slate-800/40 text-slate-400 hover:border-orange-500/40 hover:text-orange-300 text-xs font-mono transition-all"
              >
                <Github className="w-3.5 h-3.5" />
                Jenkinsfile (openclaw-msa)
              </a>
            </div>
          </div>

          {/* 아키텍처 플로우 — 모바일에서 가로 스크롤 */}
          <div className="mb-10 overflow-x-auto pb-2">
            <div className="flex items-center gap-2 text-xs text-slate-400 min-w-max sm:min-w-0 sm:flex-wrap sm:justify-center">
              {["Git Push", "Jenkins CI", "Docker Build", "minikube load", "K8s Deploy", "Caddy HTTPS", "k-devops.duckdns.org"].map((step, i, arr) => (
                <span key={step} className="flex items-center gap-2">
                  <span className="px-3 py-1.5 rounded-full border border-slate-600/50 bg-slate-800/50 text-slate-300 font-mono whitespace-nowrap">
                    {step}
                  </span>
                  {i < arr.length - 1 && <ChevronRight className="w-3.5 h-3.5 text-slate-600 shrink-0" />}
                </span>
              ))}
            </div>
          </div>

          {/* 카테고리 필터 */}
          <div className="flex flex-wrap gap-2 justify-center mb-8">
            {categories.map(cat => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                className={`flex items-center gap-1.5 px-4 py-1.5 rounded-full text-sm font-medium transition-all border ${
                  activeCategory === cat
                    ? "border-blue-500/50 bg-blue-500/20 text-blue-300"
                    : "border-slate-700/50 bg-slate-800/30 text-slate-400 hover:border-slate-600 hover:text-slate-300"
                }`}
              >
                {CATEGORY_ICONS[cat]}
                {cat}
                <span className="text-xs opacity-60">
                  {cat === "전체" ? ITEMS.length : ITEMS.filter(i => i.category === cat).length}
                </span>
              </button>
            ))}
          </div>

          {/* 카드 그리드 */}
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {visible.map((item, idx) => (
              <button
                key={item.id}
                onClick={() => setSelected(item)}
                className="glass-card p-5 rounded-xl text-left hover:border-blue-500/30 hover:shadow-lg hover:shadow-blue-500/5 transition-all group"
                style={{ animationDelay: `${idx * 50}ms` }}
              >
                <div className="flex items-start justify-between mb-3">
                  <span className={`flex items-center gap-1.5 text-xs px-2 py-1 rounded font-mono ${CATEGORY_COLORS[item.category]}`}>
                    {CATEGORY_ICONS[item.category]}
                    {item.category}
                  </span>
                  <ChevronRight className="w-4 h-4 text-slate-600 group-hover:text-blue-400 group-hover:translate-x-0.5 transition-all" />
                </div>

                <div className="flex items-start gap-2 mb-2">
                  <span className="text-blue-400 mt-0.5 shrink-0">{item.icon}</span>
                  <h3 className="text-sm font-bold text-white leading-snug">{item.title}</h3>
                </div>

                <p className="text-xs text-slate-500 leading-relaxed">{item.summary}</p>

                {item.problem && (
                  <div className="mt-3 flex items-center gap-1 text-xs text-amber-500/70">
                    <AlertTriangle className="w-3 h-3 shrink-0" />
                    <span className="truncate">{item.problem.split('.')[0]}</span>
                  </div>
                )}

                <div className="mt-3 flex flex-wrap gap-1">
                  {item.tags.slice(0, 3).map(tag => (
                    <span key={tag} className="text-xs bg-slate-800/60 text-slate-500 px-1.5 py-0.5 rounded font-mono">
                      {tag}
                    </span>
                  ))}
                  {item.tags.length > 3 && (
                    <span className="text-xs text-slate-600">+{item.tags.length - 3}</span>
                  )}
                </div>
              </button>
            ))}
          </div>

          {/* 펼치기/접기 */}
          {hasMore && (
            <div className="text-center mt-6">
              <button
                onClick={() => setExpanded(!expanded)}
                className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full border border-slate-700/50 bg-slate-800/30 text-slate-400 hover:border-blue-500/40 hover:text-blue-300 text-sm font-medium transition-all"
              >
                {expanded ? (
                  <><ChevronUp className="w-4 h-4" /> 접기</>
                ) : (
                  <><ChevronDown className="w-4 h-4" /> {filtered.length - INITIAL_COUNT}개 더 보기</>
                )}
              </button>
            </div>
          )}

          <p className="text-center mt-6 text-xs text-slate-600">
            카드를 클릭하면 상세 내용을 확인할 수 있습니다
          </p>
        </div>
      </div>

      {/* 상세 모달 */}
      <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
        <DialogContent className="w-[calc(100vw-2rem)] sm:max-w-2xl max-h-[85vh] overflow-y-auto bg-[#0a1628] border-slate-700/50 p-4 sm:p-6">
          {selected && (
            <>
              <DialogHeader>
                <div className="flex items-center gap-2 mb-1">
                  <span className={`flex items-center gap-1.5 text-xs px-2 py-1 rounded font-mono ${CATEGORY_COLORS[selected.category]}`}>
                    {CATEGORY_ICONS[selected.category]}
                    {selected.category}
                  </span>
                </div>
                <DialogTitle className="text-white text-xl leading-snug">
                  {selected.title}
                </DialogTitle>
                <p className="text-slate-400 text-sm">{selected.summary}</p>
              </DialogHeader>

              <div className="space-y-5 mt-2">
                {selected.problem && (
                  <div>
                    <div className="flex items-center gap-1.5 text-sm font-semibold text-amber-400 mb-2">
                      <AlertTriangle className="w-4 h-4" /> 문제 상황
                    </div>
                    <p className="text-sm text-slate-400 leading-relaxed">{selected.problem}</p>
                  </div>
                )}

                {(selected.before || selected.after) && (
                  <div className="grid gap-3">
                    {selected.before && <CodeBlock label="// Before (문제)" code={selected.before} tone="before" />}
                    {selected.after && <CodeBlock label="// After (해결)" code={selected.after} tone="after" />}
                  </div>
                )}

                <div>
                  <div className="flex items-center gap-1.5 text-sm font-semibold text-green-400 mb-2">
                    <CheckCircle2 className="w-4 h-4" /> 원인 & 해결
                  </div>
                  <p className="text-sm text-slate-400 leading-relaxed whitespace-pre-line">{selected.solution}</p>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-2 border-t border-slate-800">
                  {selected.tags.map(tag => (
                    <span key={tag} className="text-xs bg-slate-800/80 text-slate-400 px-2 py-1 rounded font-mono border border-slate-700/50">
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </section>
  );
}
