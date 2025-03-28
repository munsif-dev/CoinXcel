pipeline {
    agent any

    tools {
        maven 'M3'  // Maven tool name configured in Jenkins
        jdk 'JDK'   // JDK tool name configured in Jenkins
    }

    environment {
        JAVA_HOME = "${tool 'JDK'}"  // Set JAVA_HOME to the JDK tool
                
        DOCKER_HUB_USER = 'munsifahamed' // DockerHub username
        DOCKER_HUB_PASS = credentials('dockerhub-credentials')  // DockerHub password
        AWS_CREDENTIALS = credentials('aws-credentials')  // AWS credentials for EC2
        COINXCEL_REPO = 'https://github.com/munsif-dev/CoinXcel.git'  // GitHub repository URL
        MYSQL_Credentials = credentials('mysql-credentials')  // MySQL credentials
        HOST = '3.84.235.189'  // EC2 instance IP address
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout your code from GitHub
                git branch: 'main', url: "${env.COINXCEL_REPO}"
            }
        }

        stage('Set Up MySQL') {
            steps {
                script {
                    // Start MySQL service using Docker Compose
                    sh 'docker-compose -f docker-compose.yml up -d mysql'
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    // Build the Spring Boot app image using docker-compose (from the docker-compose.yml file)
                    sh 'docker-compose -f docker-compose.yml build springboot'

                  
                     // Securely login to DockerHub using credentials stored in Jenkins
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PASS')]) {
                        sh '''
                            echo $DOCKER_HUB_PASS | docker login -u $DOCKER_HUB_USER --password-stdin
                        '''
                    }

                    // Push the image to DockerHub
                    sh 'docker push $DOCKER_HUB_USER/springboot-app:latest'
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                script {
                    // SSH into your EC2 instance and pull the latest Docker image
                    sshagent(['aws-credentials']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no ubuntu@${HOST} "docker pull $DOCKER_HUB_USER/springboot-app:latest"
                            ssh -o StrictHostKeyChecking=no ubuntu@${HOST} "docker-compose -f docker-compose.yml up -d"
                        '''
                    }
                }
            }
        }

        stage('Tear Down MySQL') {
            steps {
                script {
                    // Shut down MySQL container after the tests
                    sh 'docker-compose down'
                }
            }
        }
    }

    post {
        success {
            echo 'Build and deployment were successful!'
        }
        failure {
            echo 'There was a failure during the build or deployment process.'
        }
    }
}
