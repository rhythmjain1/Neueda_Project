pipeline {
    agent any

    environment {
        REGISTRY      = 'your-registry.example.com'
        IMAGE_BACKEND = "${REGISTRY}/tms-backend"
        IMAGE_FRONTEND= "${REGISTRY}/tms-frontend"
        IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'local'}"
        
        // EC2 Deployment Variables
        EC2_HOST      = 'ec2-user@your-ec2-ip.compute.amazonaws.com'
        DEPLOY_DIR    = '/opt/tms-app'
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

        stage('Deploy to EC2') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to Amazon EC2...'
                // Requires the "SSH Agent" Jenkins plugin and a credential named 'ec2-ssh-key'
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh '''
                        # 1. Copy the docker-compose file to the EC2 server
                        scp -o StrictHostKeyChecking=no docker-compose.yml ${EC2_HOST}:${DEPLOY_DIR}/docker-compose.yml
                        
                        # 2. SSH into EC2, pull latest images, and restart containers
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} "
                            cd ${DEPLOY_DIR}
                            export IMAGE_TAG=${IMAGE_TAG}
                            docker compose pull
                            docker compose up -d --remove-orphans
                            docker compose ps
                        "
                    '''
                }
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
