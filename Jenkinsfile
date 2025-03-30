pipeline {
    agent any

    tools {
        maven 'M3'  // Maven tool name configured in Jenkins
        jdk 'JDK'   // JDK tool name configured in Jenkins
    }

    environment {
        JAVA_HOME = "${tool 'JDK'}"  // Set JAVA_HOME to the JDK tool
                
        DOCKER_HUB_USER = 'munsifahamed' // DockerHub username
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')  // DockerHub credentials
        AWS_CREDENTIALS = credentials('aws-credentials')  // AWS credentials for EC2
        COINXCEL_REPO = 'https://github.com/munsif-dev/CoinXcel.git'  // GitHub repository URL
        MYSQL_CREDENTIALS = credentials('mysql-credentials')  // MySQL credentials
        EC2_HOST = '3.84.235.189'  // EC2 instance IP address
        SSH_KEY_CREDENTIALS = 'aws-ssh-key'  // Jenkins credential ID for SSH key
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout your code from GitHub
                git branch: 'main', url: "${env.COINXCEL_REPO}"
                
                // Create ansible directory if it doesn't exist
                sh 'mkdir -p ansible'
                
                // Create ansible playbooks
                writeFile file: 'ansible/install-docker.yml', text: '''
---
- name: Install Docker and Docker Compose on EC2
  hosts: coinxcel_servers
  become: yes
  tasks:
    - name: Update apt cache
      apt:
        update_cache: yes
        cache_valid_time: 3600

    - name: Install required packages
      apt:
        name:
          - ca-certificates
          - curl
          - gnupg
          - lsb-release
        state: present

    - name: Add Docker GPG key
      apt_key:
        url: https://download.docker.com/linux/ubuntu/gpg
        state: present

    - name: Add Docker repository
      apt_repository:
        repo: deb [arch=amd64] https://download.docker.com/linux/ubuntu {{ ansible_distribution_release }} stable
        state: present

    - name: Install Docker Engine
      apt:
        name:
          - docker-ce
          - docker-ce-cli
          - containerd.io
        state: present
        update_cache: yes

    - name: Install Docker Compose
      get_url:
        url: https://github.com/docker/compose/releases/download/v2.18.1/docker-compose-linux-x86_64
        dest: /usr/local/bin/docker-compose
        mode: '0755'

    - name: Create docker group
      group:
        name: docker
        state: present

    - name: Add ubuntu user to docker group
      user:
        name: ubuntu
        groups: docker
        append: yes

    - name: Start and enable Docker service
      service:
        name: docker
        state: started
        enabled: yes
'''

                writeFile file: 'ansible/deploy-app.yml', text: '''
---
- name: Deploy CoinXcel Application
  hosts: coinxcel_servers
  become: yes
  vars:
    docker_hub_user: "{{ lookup('env', 'DOCKER_HUB_USER') }}"
    docker_hub_password: "{{ lookup('env', 'DOCKER_HUB_CREDS_PSW') }}"
    mysql_user: "{{ lookup('env', 'MYSQL_CREDENTIALS_USR') }}"
    mysql_password: "{{ lookup('env', 'MYSQL_CREDENTIALS_PSW') }}"
  tasks:
    - name: Create app directory
      file:
        path: /home/ubuntu/coinxcel
        state: directory
        owner: ubuntu
        group: ubuntu
        mode: '0755'

    - name: Copy docker-compose file
      copy:
        src: ../docker-compose.yml
        dest: /home/ubuntu/coinxcel/docker-compose.yml
        owner: ubuntu
        group: ubuntu
        mode: '0644'

    - name: Login to Docker Hub
      command: docker login -u {{ docker_hub_user }} -p {{ docker_hub_password }}
      become: yes
      become_user: ubuntu
      no_log: true

    - name: Pull latest Docker images
      command: docker pull {{ docker_hub_user }}/springboot-app:latest
      become: yes
      become_user: ubuntu

    - name: Stop existing containers
      command: docker-compose -f /home/ubuntu/coinxcel/docker-compose.yml down
      become: yes
      become_user: ubuntu
      ignore_errors: yes

    - name: Start application with docker-compose
      command: docker-compose -f /home/ubuntu/coinxcel/docker-compose.yml up -d
      become: yes
      become_user: ubuntu
      environment:
        MYSQL_USER: "{{ mysql_user }}"
        MYSQL_PASSWORD: "{{ mysql_password }}"

    - name: Wait for application to start
      pause:
        seconds: 30

    - name: Check container status
      command: docker ps
      become: yes
      become_user: ubuntu
      register: container_status

    - name: Display container status
      debug:
        var: container_status.stdout_lines
'''

                writeFile file: 'ansible/hosts', text: '''
[coinxcel_servers]
${EC2_HOST} ansible_user=ubuntu ansible_ssh_private_key_file=/tmp/ec2_key.pem ansible_ssh_common_args='-o StrictHostKeyChecking=no'
'''
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
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
                    // Build the Spring Boot app image using docker-compose
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
                    // Copy SSH key to a temporary location
                    withCredentials([sshUserPrivateKey(credentialsId: env.SSH_KEY_CREDENTIALS, keyFileVariable: 'SSH_KEY')]) {
                        sh 'mkdir -p /tmp'
                        sh 'cp $SSH_KEY /tmp/ec2_key.pem'
                        sh 'chmod 600 /tmp/ec2_key.pem'
                        
                        // Use Ansible to install Docker
                        sh 'ansible-playbook -i ansible/hosts ansible/install-docker.yml'
                        
                        // Use Ansible to deploy the application
                        withEnv([
                            "DOCKER_HUB_USER=${env.DOCKER_HUB_USER}",
                            "MYSQL_CREDENTIALS_USR=${env.MYSQL_CREDENTIALS_USR}",
                            "MYSQL_CREDENTIALS_PSW=${env.MYSQL_CREDENTIALS_PSW}"
                        ]) {
                            withCredentials([
                                string(credentialsId: 'dockerhub-credentials', variable: 'DOCKER_HUB_CREDS_PSW')
                            ]) {
                                sh 'ansible-playbook -i ansible/hosts ansible/deploy-app.yml'
                            }
                        }
                        
                        // Remove SSH key after deployment
                        sh 'rm -f /tmp/ec2_key.pem'
                    }
                }
            }
        }

        stage('Tear Down Local MySQL') {
            steps {
                script {
                    // Shut down MySQL container after the tests
                    sh 'docker-compose -f docker-compose.yml down'
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
        always {
            script {
                // Clean up Docker resources
                sh 'docker logout || true'
                sh 'docker system prune -f || true'
                
                // Clean workspace
                cleanWs()
            }
        }
    }
}