pipeline {
    agent any

    tools {
        maven 'Maven_3_9_9'
        jdk 'JDK 17'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production'], description: 'Select deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8081', description: 'Port for the application to run on EC2')
        string(name: 'MYSQL_PORT', defaultValue: '3306', description: 'Port for MySQL to run on EC2')
    }

    environment {
        JWT_SECRET = credentials('jwt-secret')
        DB_CREDENTIALS = credentials('db-credentials')
        MYSQL_ROOT_PASSWORD = credentials('mysql-root-password')
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = "shamilkaleel/blog-app-backend"
        EC2_HOST = credentials('ec2-host')
        EC2_USER = 'ubuntu'
        DEPLOY_ENV = "${params.DEPLOY_ENV ?: 'staging'}"
        SERVER_PORT = "${params.SERVER_PORT}"
        MYSQL_PORT = "${params.MYSQL_PORT}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests --no-transfer-progress'
            }
        }

//         stage('Test') {
//             steps {
//                 withEnv([
//                     'SPRING_PROFILES_ACTIVE=test',
//                     'SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb',
//                     'SPRING_DATASOURCE_USERNAME=sa',
//                     'SPRING_DATASOURCE_PASSWORD=',
//                     'SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop'
//                 ]) {
//                     sh './mvnw test'
//                 }
//             }
//             post {
//                 always {
//                     junit '**/target/surefire-reports/*.xml'
//                 }
//             }
//         }

        stage('Prepare .env File') {
            steps {
                script {
                    sh '''
                        # Create .env file with secure permissions
                        touch .env && chmod 600 .env

                        cat > .env << EOL
SPRING_APPLICATION_NAME=blog-app
SERVER_PORT=${SERVER_PORT}
MYSQL_PORT=${MYSQL_PORT}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/dental
SPRING_DATASOURCE_USERNAME=${DB_CREDENTIALS_USR}
SPRING_DATASOURCE_PASSWORD=${DB_CREDENTIALS_PSW}
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_SHOW_SQL=false
APP_CORS_ALLOWED_ORIGINS=*
SPRING_APP_JWTSECRET=${JWT_SECRET}
SPRING_APP_JWTEXPIRATIONMS=86400000
SPRING_APP_JWTCOOKIENAME=token
EOL

                        # Verify file was created successfully
                        if [ ! -f .env ]; then
                            echo "Failed to create .env file"
                            exit 1
                        fi
                    '''
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                    sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh 'echo $DOCKER_CREDENTIALS_PSW | docker login -u $DOCKER_CREDENTIALS_USR --password-stdin'
                sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy to EC2') {
            steps {
                withEnv([
                    "REMOTE_USER=${EC2_USER}",
                    "REMOTE_HOST=${EC2_HOST}",
                    "DOCKER_USERNAME=${DOCKER_CREDENTIALS_USR}",
                    "DOCKER_PASSWORD=${DOCKER_CREDENTIALS_PSW}"
                ]) {
                    sshagent(['ec2-ssh-key']) {
                        sh '''
                            # Create deployment directory
                            echo "Creating deployment directory..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p ~/app-deployment"

                            # Copy deployment files
                            echo "Copying deployment files..."
                            scp -o StrictHostKeyChecking=no docker-compose.yml .env $REMOTE_USER@$REMOTE_HOST:~/app-deployment/

                            # Execute deployment commands
                            echo "Starting deployment..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST bash << 'EOF'
                                cd ~/app-deployment

                                # Ensure Docker permissions are correct
                                sudo usermod -aG docker ubuntu

                                # Docker login
                                echo "$DOCKER_PASSWORD" | sudo docker login --username "$DOCKER_USERNAME" --password-stdin

                                # Stop existing containers
                                sudo docker-compose down --remove-orphans || true

                                # Clean up old resources
                                sudo docker rm -f $(sudo docker ps -a -q) 2>/dev/null || true
                                sudo docker network prune -f || true

                                # Pull latest images
                                sudo docker-compose pull

                                # Start containers
                                sudo docker-compose up -d



                                # Check container status
                                if ! sudo docker-compose ps | grep -q "Up"; then
                                    echo "Containers failed to start properly"
                                    sudo docker-compose logs
                                    exit 1
                                fi

                                # Print container status
                                echo "Container status:"
                                sudo docker-compose ps
                                echo "Deployment completed successfully!"
EOF
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Clean up Docker resources
                sh 'docker logout || true'
                sh 'docker system prune -f || true'

                // Remove sensitive files
                sh '''
                    rm -f .env
                    rm -f get-docker.sh || true
                '''

                cleanWs()
            }
        }
        success {
            echo "Successfully deployed to ${DEPLOY_ENV} environment at ${EC2_HOST}"
        }
        failure {
            echo "Deployment to ${DEPLOY_ENV} failed"
        }
    }
}