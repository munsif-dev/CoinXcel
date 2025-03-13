pipeline {
    agent any

    tools {
        maven 'M3' 
        jdk 'JDK' 
    }

    environment {
        JAVA_HOME = "${tool 'JDK'}"
        REPO_NAME = 'munsifahamed' 
        DOCKER_HUB_USER = credentials('dockerhub-credentials') 
        DOCKER_HUB_PASS = credentials('dockerhub-credentials')  
        AWS_CREDENTIALS = credentials('aws-credentials')  
        FRONTEND_REPO = 'https://github.com/munsif-dev/CoinXcelClient.git'
        BACKEND_REPO = 'https://github.com/munsif-dev/CoinXcel.git'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Initialize') {
            steps {
                // Using WSL for echo command
                sh 'echo Starting Build'
            }
        }

        stage('Clean') {
            steps {
                // Running Maven clean in WSL (Linux shell)
                sh 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                // Running Maven compile in WSL (Linux shell)
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                // Running Maven test in WSL (Linux shell)
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                // Running Maven package in WSL (Linux shell)
                sh 'mvn package'
            }
        }

        stage('Build Docker Image') {
            steps {
                // Using WSL to build the Docker image
                sh 'docker build -t coinxcel .'
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    // Using WSL to build Docker images and push to DockerHub
                    sh "docker build -t ${REPO_NAME}/coinxcel-server ./backend"
                    sh "docker build -t ${REPO_NAME}/coinxcel-client ./frontend"
                    sh "docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS"
                    sh "docker push ${REPO_NAME}/coinxcel-server"
                    sh "docker push ${REPO_NAME}/coinxcel-client"
                }
            }
        }

        stage('Provision Infrastructure with Terraform') {
            steps {
                // Using WSL to run Terraform commands
                sh '''
                cd terraform
                terraform init
                terraform apply -auto-approve
                '''
            }
        }

        stage('Configure & Deploy with Ansible') {
            steps {
                // Using WSL to run Ansible playbook
                sh '''
                cd ansible
                ansible-playbook -i inventory deploy.yml
                '''
            }
        }
    }

    post {
        success {
            // Success message using WSL shell
            sh 'echo Build succeeded'
        }

        failure {
            // Failure message using WSL shell
            sh 'echo Build failed'
        }

        always {
            // Cleanup using WSL shell
            sh 'echo Cleaning up...'
            cleanWs()
        }
    }
}
