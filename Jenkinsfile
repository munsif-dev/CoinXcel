pipeline {
    agent any

    tools {
        maven 'M3' // Ensure this matches the name of Maven in your Jenkins configuration
        jdk 'JDK' // Ensure this matches the name of JDK in your Jenkins configuration
    }

    environment {
        JAVA_HOME = "${tool 'JDK'}"
    }

    stages {
        stage('Initialize') {
            steps {
                sh 'echo "Starting Build"'
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Deploy') {
            steps {
                sh 'echo "Deploying application"'
                // Add deployment steps here
            }
        }
    }

    post {
        success {
            sh 'echo "Build succeeded"'
        }

        failure {
            sh 'echo "Build failed"'
        }

        always {
            sh 'echo "Cleaning up..."'
            cleanWs()
        }
    }
}
