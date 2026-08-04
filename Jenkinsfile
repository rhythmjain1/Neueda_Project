pipeline {
    agent any

    environment {
        REGISTRY      = 'your-registry.example.com'
        IMAGE_BACKEND = "${REGISTRY}/tms-backend"
        IMAGE_FRONTEND= "${REGISTRY}/tms-frontend"
        IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'local'}"
        
        DEPLOY_DIR    = "${env.WORKSPACE}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out source...'
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    echo '🔨 Building Spring Boot application...'
                    sh 'mvn clean package -DskipTests -B -q'
                }
            }
        }

        stage('Test Backend') {
            steps {
                dir('backend') {
                    echo '🧪 Running backend tests...'
                    sh 'mvn test -B'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    echo '⚡ Installing frontend dependencies...'
                    sh 'npm ci --silent'
                    echo '📦 Building frontend production bundle...'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker images...'
                parallel(
                    backend: {
                        sh "docker build -t ${IMAGE_BACKEND}:${IMAGE_TAG} -t ${IMAGE_BACKEND}:latest ./backend"
                    },
                    frontend: {
                        sh "docker build -t ${IMAGE_FRONTEND}:${IMAGE_TAG} -t ${IMAGE_FRONTEND}:latest ./frontend"
                    }
                )
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                echo '📤 Pushing Docker images to registry...'
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh "echo $DOCKER_PASS | docker login ${REGISTRY} -u $DOCKER_USER --password-stdin"
                    sh "docker push ${IMAGE_BACKEND}:${IMAGE_TAG}"
                    sh "docker push ${IMAGE_BACKEND}:latest"
                    sh "docker push ${IMAGE_FRONTEND}:${IMAGE_TAG}"
                    sh "docker push ${IMAGE_FRONTEND}:latest"
                }
            }
        }

        stage('Deploy Locally') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying services locally on this host...'
                sh """
                    cd ${DEPLOY_DIR}
                    export IMAGE_TAG=${IMAGE_TAG}
                    docker compose up -d --remove-orphans
                    docker compose ps
                """
                echo '✅ Services running on localhost: frontend=3000  backend=8080  mysql=3306'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        always {
            sh 'docker system prune -f --filter "until=24h" || true'
        }
    }
}
