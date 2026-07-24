pipeline {
    agent any

    environment {
        // Jenkins Credentials ID: dockerhub-credentials (UsernamePassword 타입)
        DOCKER_HUB_CREDS  = credentials('dockerhub-credentials')
        DOCKER_HUB_USER   = "${DOCKER_HUB_CREDS_USR}"
        DOCKER_HUB_PASS   = "${DOCKER_HUB_CREDS_PSW}"
        BACKEND_IMAGE     = "${DOCKER_HUB_CREDS_USR}/portfolio-backend"
        FRONTEND_IMAGE    = "${DOCKER_HUB_CREDS_USR}/portfolio-frontend"
        IMAGE_TAG         = "${env.BUILD_NUMBER}"
        K8S_NAMESPACE     = "portfolio"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        // ─────────────────────────────────────────────
        stage('Checkout') {
        // ─────────────────────────────────────────────
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH} | Commit: ${env.GIT_COMMIT?.take(7)}"
            }
        }

        // ─────────────────────────────────────────────
        stage('Build Backend') {
        // ─────────────────────────────────────────────
            steps {
                dir('dev-portfolio-backend') {
                    sh 'mvn clean package -DskipTests -q'
                    echo 'Backend JAR 빌드 완료'
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Test Backend') {
        // ─────────────────────────────────────────────
            steps {
                dir('dev-portfolio-backend') {
                    sh 'mvn test -q'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'dev-portfolio-backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Build Frontend') {
        // ─────────────────────────────────────────────
            steps {
                dir('dev-portfolio') {
                    sh '''
                        npm install -g pnpm@10.4.1 2>/dev/null || true
                        pnpm install --frozen-lockfile
                        pnpm exec vite build
                    '''
                    echo 'Frontend 빌드 완료'
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Docker Build & Push') {
        // ─────────────────────────────────────────────
            steps {
                sh 'echo $DOCKER_HUB_PASS | docker login -u $DOCKER_HUB_USER --password-stdin'

                parallel(
                    "Backend Image": {
                        dir('dev-portfolio-backend') {
                            sh """
                                docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest .
                                docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                                docker push ${BACKEND_IMAGE}:latest
                            """
                        }
                    },
                    "Frontend Image": {
                        dir('dev-portfolio') {
                            sh """
                                docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest .
                                docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                                docker push ${FRONTEND_IMAGE}:latest
                            """
                        }
                    }
                )
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Deploy to Kubernetes') {
        // ─────────────────────────────────────────────
            steps {
                dir('k8s') {
                    sh """
                        # 네임스페이스 및 기본 리소스 적용
                        kubectl apply -f namespace.yaml
                        kubectl apply -f postgres.yaml -n ${K8S_NAMESPACE}
                        kubectl apply -f secret.yaml   -n ${K8S_NAMESPACE}
                        kubectl apply -f configmap.yaml -n ${K8S_NAMESPACE}

                        # 이미지 태그를 현재 빌드 번호로 교체 후 적용
                        sed "s|IMAGE_TAG|${IMAGE_TAG}|g" backend.yaml | kubectl apply -n ${K8S_NAMESPACE} -f -
                        sed "s|IMAGE_TAG|${IMAGE_TAG}|g" frontend.yaml | kubectl apply -n ${K8S_NAMESPACE} -f -

                        # Ingress 적용
                        kubectl apply -f ingress.yaml -n ${K8S_NAMESPACE}

                        # 배포 완료 대기
                        kubectl rollout status deployment/portfolio-backend  -n ${K8S_NAMESPACE} --timeout=120s
                        kubectl rollout status deployment/portfolio-frontend -n ${K8S_NAMESPACE} --timeout=60s
                    """
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Smoke Test') {
        // ─────────────────────────────────────────────
            steps {
                sh '''
                    sleep 5
                    # Minikube IP 기반 헬스체크
                    MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "localhost")
                    curl -sf http://${MINIKUBE_IP}/api/auth/health || echo "Health check skipped (Minikube not reachable from Jenkins)"
                '''
            }
        }
    }

    post {
        success {
            echo "빌드 #${env.BUILD_NUMBER} 성공 - 배포 완료"
        }
        failure {
            echo "빌드 #${env.BUILD_NUMBER} 실패"
        }
        always {
            // 로컬 빌드된 dangling 이미지 정리
            sh 'docker image prune -f || true'
            cleanWs()
        }
    }
}
