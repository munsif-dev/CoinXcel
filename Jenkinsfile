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
        BACKEND_REPO = 'https://github.com/munsif-dev/CoinXcel.git'
        MYSQL_Credentials = credentials('mysql-credentials')
        MYSQL_USER = 'root'  // MySQL username
        MYSQL_PASS = credentials('mysql-root-password')  // MySQL password
        MYSQL_HOST = '3.84.235.189'                         //credentials('mysql-ec2')  // MySQL host
    }

    triggers {
        githubPush()  // Trigger the pipeline on each commit to the backend repository
    }

    stages {
        stage('Initialize') {
            steps {
                // Log that the pipeline has started
                sh 'echo Starting Build'
            }
        }

        stage('Clone Repository') {
            steps {
                // Clone the backend repository
                git url: "${BACKEND_REPO}", branch: 'main'
            }
        }

        stage('Clean') {
            steps {
                // Clean previous builds using Maven
                sh 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                // Compile the backend project using Maven
                sh 'mvn compile'
            }
        }


        stage('Package') {
            steps {
                // Package the backend application
                sh 'mvn package'
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    sh """
    docker build --build-arg MYSQL_HOST=${MYSQL_HOST} --build-arg MYSQL_USER=${MYSQL_USER} --build-arg MYSQL_PASS=${MYSQL_PASS} -t ${REPO_NAME}/coinxcel-server .
"""
                    // Log in to Docker Hub and push the image
                    sh "docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS"
                    sh "docker push ${REPO_NAME}/coinxcel-server"
                }
            }
        }

        stage('Provision EC2 Instances with Terraform') {
            steps {
                // Run Terraform to provision EC2 instances, or update them if already existing
                sh '''
                cd terraform
                terraform init
                terraform apply -auto-approve
                '''
            }
        }

        stage('Install Docker on EC2 Instances') {
            steps {
                // Install Docker on the EC2 instances using Ansible
                sh '''
                cd ansible
                ansible-playbook -i inventory install-docker.yml
                '''
            }
        }

        stage('Deploy to EC2 Instances') {
            steps {
                // Deploy the Spring Boot Docker container to the EC2 instance
                sh '''
                cd ansible
                ansible-playbook -i inventory deploy.yml
                '''
            }
        }
    }

    post {
        success {
            // Log success message
            sh 'echo Build succeeded'
        }

        failure {
            // Log failure message
            sh 'echo Build failed'
        }

        always {
            // Cleanup workspace after the build
            cleanWs()
        }
    }
}
