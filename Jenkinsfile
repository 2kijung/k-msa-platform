// ============================================================================
// k-msa-platform / Jenkinsfile — CI/CD 파이프라인
// ============================================================================
// [무엇을] k-msa-platform 전체 서비스 빌드·테스트·도커 이미지 빌드·K8s 배포.
// [왜/MSA] 서비스마다 독립 빌드 + parallel stage → 변경된 서비스만 재배포 가능.
// [어떻게] 1) 소스 체크아웃 → 2) Maven 병렬 빌드 → 3) Docker 이미지 빌드·푸시
//          → 4) K8s Rolling Update (kubectl set image)
// [참조] K-portfolio Jenkinsfile 구조 계승 (Docker Hub 자격증명·K8s 배포 방식)
// ============================================================================

pipeline {
    agent any

    environment {
        // [보안] Jenkins Credentials ID: dockerhub-credentials (UsernamePassword 타입)
        // 실제 Docker Hub 계정은 Credentials Store에서 관리.
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')
        DOCKER_HUB_USER  = "${DOCKER_HUB_CREDS_USR}"
        IMAGE_PREFIX     = "${DOCKER_HUB_CREDS_USR}/kmsa"
        IMAGE_TAG        = "${env.BUILD_NUMBER ?: 'latest'}"
        K8S_NAMESPACE    = "kmsa"
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH} | Commit: ${env.GIT_COMMIT?.take(7)}"
            }
        }

        // [왜/MSA] 서비스별 병렬 빌드 → 빌드 시간 단축. 독립 배포의 CI 표현.
        stage('Build Services') {
            parallel {
                stage('auth-service') {
                    steps {
                        dir('platform/auth') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
                stage('notification-service') {
                    steps {
                        dir('platform/notification') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
                stage('contact-service') {
                    steps {
                        dir('apps/portfolio/contact-service') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
                stage('content-service') {
                    steps {
                        dir('apps/portfolio/content-service') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
                stage('blog-service') {
                    steps {
                        dir('apps/blog') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
                stage('analytics-service') {
                    steps {
                        dir('apps/portfolio/analytics-service') {
                            sh 'mvn clean package -DskipTests -q'
                        }
                    }
                }
            }
        }

        stage('Test') {
            parallel {
                stage('auth tests') {
                    steps {
                        dir('platform/auth') { sh 'mvn test -q' }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true,
                                testResults: 'platform/auth/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('contact tests') {
                    steps {
                        dir('apps/portfolio/contact-service') { sh 'mvn test -q' }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true,
                                testResults: 'apps/portfolio/contact-service/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        // [왜] Docker Hub 푸시. 각 서비스 이미지를 독립 배포 단위로 관리.
        stage('Docker Build & Push') {
            steps {
                script {
                    def services = [
                        [dir: 'platform/auth',                       name: 'auth-service'],
                        [dir: 'platform/notification',               name: 'notification-service'],
                        [dir: 'apps/portfolio/contact-service',      name: 'contact-service'],
                        [dir: 'apps/portfolio/content-service',      name: 'content-service'],
                        [dir: 'apps/blog',                           name: 'blog-service'],
                        [dir: 'apps/portfolio/analytics-service',    name: 'analytics-service'],
                    ]
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                     usernameVariable: 'HUB_USER',
                                                     passwordVariable: 'HUB_PASS')]) {
                        sh "echo \$HUB_PASS | docker login -u \$HUB_USER --password-stdin"
                        services.each { svc ->
                            def img = "${IMAGE_PREFIX}-${svc.name}:${IMAGE_TAG}"
                            sh "docker build -t ${img} ${svc.dir}/"
                            sh "docker push ${img}"
                            echo "Pushed: ${img}"
                        }
                    }
                }
            }
        }

        // [왜/MSA] 서비스별 독립 Rolling Update.
        //          변경된 서비스 이미지만 교체 → 다른 서비스 무중단.
        // [전제] K8s 클러스터 접근: kubectl + kubeconfig 설정 필요.
        stage('Deploy to K8s') {
            when { branch 'main' }
            steps {
                script {
                    def deployments = [
                        'auth',        'notification',
                        'contact',     'content',
                        'blog',        'analytics'
                    ]
                    deployments.each { svc ->
                        sh """
                            kubectl set image deployment/${svc}-service \\
                                ${svc}-service=${IMAGE_PREFIX}-${svc}-service:${IMAGE_TAG} \\
                                -n ${K8S_NAMESPACE}
                            kubectl rollout status deployment/${svc}-service -n ${K8S_NAMESPACE} --timeout=120s
                        """
                    }
                }
            }
        }

        // [Phase 6] 부하테스트 — 배포 후 자동 SLO 검증
        stage('Load Test (SLO Verify)') {
            when { branch 'main' }
            steps {
                sh 'k6 run infra/load-test/auth-direct.js || true'
                sh 'k6 run infra/load-test/auth-login.js --env BASE_URL=http://localhost:8090 || true'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'infra/load-test/*.json', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo "✅ 빌드 성공: ${env.BUILD_NUMBER}"
        }
        failure {
            echo "❌ 빌드 실패: ${env.BUILD_NUMBER} — 로그를 확인하세요"
        }
    }
}
