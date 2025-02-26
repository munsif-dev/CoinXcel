pipeline {
    agent any

    tools {
        maven 'M3' // Make sure this matches the name of Maven in your Jenkins configuration
        jdk 'JDK' // Make sure this matches the name of JDK in your Jenkins configuration
    }

    environment {
        JAVA_HOME = "${tool 'JDK'}"
        REPO_NAME = 'munsifahamed'  // Your DockerHub repository name
        DOCKER_HUB_USER = credentials('dockerhub-credentials')  // Store DockerHub credentials in Jenkins
        DOCKER_HUB_PASS = credentials('dockerhub-credentials')  // Store DockerHub credentials in Jenkins
        AWS_CREDENTIALS = credentials('aws-credentials')  // Store AWS credentials in Jenkins
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Initialize') {
            steps {
                bat 'echo Starting Build'
            }
        }

        stage('Clean') {
            steps {
                bat 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                bat 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t coinxcel .'
            }
        }
        stage('Build & Push Docker Images') {
            steps {
                script {
                    bat "docker build -t ${REPO_NAME}/coinxcel-server ./backend"
                    bat "docker build -t ${REPO_NAME}/coinxcel-client ./frontend"
                    bat "docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS"
                    bat "docker push ${REPO_NAME}/coinxcel-server"
                    bat "docker push ${REPO_NAME}/coinxcel-client"
                }
            }
        }

        stage('Provision Infrastructure with Terraform') {
            steps {
                bat '''
                cd terraform
                terraform init
                terraform apply -auto-approve
                '''
            }
        }

        stage('Configure & Deploy with Ansible') {
            steps {
                bat '''
                cd ansible
                ansible-playbook -i inventory deploy.yml
                '''
            }
        }
    }

    post {
        success {
            bat 'echo Build succeeded'
        }

        failure {
            bat 'echo Build failed'
        }

        always {
            bat 'echo Cleaning up...'
            cleanWs()
        }
    }
}
