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

        stage('Deploy') {
            steps {
                bat 'echo Deploying application'
                // Add deployment steps here
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
