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
  gather_facts: yes
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
          - apt-transport-https
          - python3-docker
        state: present

    - name: Create keyrings directory
      file:
        path: /etc/apt/keyrings
        state: directory
        mode: '0755'

    - name: Add Docker GPG key
      block:
        - name: Download Docker GPG key
          get_url:
            url: https://download.docker.com/linux/ubuntu/gpg
            dest: /tmp/docker-archive-keyring.gpg
            mode: '0644'
            
        - name: Dearmor GPG key
          shell: gpg --dearmor < /tmp/docker-archive-keyring.gpg > /etc/apt/keyrings/docker.gpg
          args:
            creates: /etc/apt/keyrings/docker.gpg
            
        - name: Set permissions on key
          file:
            path: /etc/apt/keyrings/docker.gpg
            mode: '0644'

    - name: Set up the repository
      apt_repository:
        repo: "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu {{ ansible_distribution_release }} stable"
        state: present
        filename: docker

    - name: Install Docker Engine
      apt:
        name:
          - docker-ce
          - docker-ce-cli
          - containerd.io
          - docker-buildx-plugin
          - docker-compose-plugin
        state: present
        update_cache: yes

    - name: Install Docker Compose standalone
      get_url:
        url: https://github.com/docker/compose/releases/download/v2.24.1/docker-compose-linux-x86_64
        dest: /usr/local/bin/docker-compose
        mode: '0755'
      register: compose_download
      
    - name: Create symbolic link for Docker Compose
      file:
        src: /usr/local/bin/docker-compose
        dest: /usr/bin/docker-compose
        state: link
      when: compose_download.changed

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
        
    - name: Verify Docker installation
      command: docker --version
      register: docker_version
      changed_when: false
      
    - name: Display Docker version
      debug:
        var: docker_version.stdout
        
    - name: Verify Docker Compose installation
      command: docker-compose --version
      register: compose_version
      changed_when: false
      
    - name: Display Docker Compose version
      debug:
        var: compose_version.stdout
'''

                writeFile file: 'ansible/ec2-deploy.yml', text: '''
---
- name: Deploy CoinXcel Application to EC2
  hosts: coinxcel_servers
  become: yes
  vars:
    docker_hub_user: "{{ lookup('env', 'DOCKER_HUB_USER') }}"
    docker_hub_password: "{{ lookup('env', 'DOCKER_HUB_CREDS_PSW') }}"
    mysql_user: "{{ lookup('env', 'MYSQL_CREDENTIALS_USR') }}"
    mysql_password: "{{ lookup('env', 'MYSQL_CREDENTIALS_PSW') }}"
  tasks:
    - name: Ensure python3-docker is installed
      apt:
        name: python3-docker
        state: present
        update_cache: yes
      
    - name: Create app directory
      file:
        path: /home/ubuntu/coinxcel
        state: directory
        owner: ubuntu
        group: ubuntu
        mode: '0755'

    - name: Copy application JAR file
      copy:
        src: ../target/CoinXcel-0.0.1-SNAPSHOT.jar
        dest: /home/ubuntu/coinxcel/CoinXcel.jar
        owner: ubuntu
        group: ubuntu
        mode: '0644'

    - name: Copy docker-compose file
      copy:
        src: ../docker-compose.yml
        dest: /home/ubuntu/coinxcel/docker-compose.yml
        owner: ubuntu
        group: ubuntu
        mode: '0644'
        
    - name: Copy Dockerfile
      copy:
        src: ../Dockerfile
        dest: /home/ubuntu/coinxcel/Dockerfile
        owner: ubuntu
        group: ubuntu
        mode: '0644'

    - name: Login to Docker Hub
      shell: echo "{{ docker_hub_password }}" | docker login -u {{ docker_hub_user }} --password-stdin
      become: yes
      become_user: ubuntu
      no_log: true
      register: login_result
      
    - name: Display Docker login result
      debug:
        msg: "Docker login successful"
      when: login_result.rc == 0
      
    - name: Display Docker login failure
      fail:
        msg: "Docker login failed"
      when: login_result.rc != 0

    - name: Stop existing containers
      shell: cd /home/ubuntu/coinxcel && docker-compose down --remove-orphans || true
      become: yes
      become_user: ubuntu
      ignore_errors: yes
      
    - name: Prune docker resources if needed
      shell: docker system prune -f
      become: yes
      become_user: ubuntu
      ignore_errors: yes

    - name: Build Docker image on EC2
      shell: cd /home/ubuntu/coinxcel && docker-compose build
      become: yes
      become_user: ubuntu
      register: build_result
      
    - name: Display build result
      debug:
        var: build_result.stdout_lines

    - name: Start application with docker-compose
      shell: cd /home/ubuntu/coinxcel && docker-compose up -d
      become: yes
      become_user: ubuntu
      environment:
        MYSQL_USER: "{{ mysql_user }}"
        MYSQL_PASSWORD: "{{ mysql_password }}"
      register: compose_up
      
    - name: Display docker-compose up result
      debug:
        var: compose_up.stdout_lines

    - name: Wait for application to start
      pause:
        seconds: 30

    - name: Check container status
      shell: docker ps -a
      become: yes
      become_user: ubuntu
      register: container_status

    - name: Display container status
      debug:
        var: container_status.stdout_lines
        
    - name: Verify MySQL container is running
      shell: docker ps | grep mysql-server
      become: yes
      become_user: ubuntu
      register: mysql_running
      failed_when: mysql_running.rc != 0
      
    - name: Verify SpringBoot container is running
      shell: docker ps | grep springboot-app
      become: yes
      become_user: ubuntu
      register: springboot_running
      failed_when: springboot_running.rc != 0
      
    - name: Check application logs if running
      shell: docker logs springboot-app --tail 50
      become: yes
      become_user: ubuntu
      register: app_logs
      when: springboot_running.rc == 0
      
    - name: Display application logs
      debug:
        var: app_logs.stdout_lines
      when: app_logs is defined
'''

                // Create ansible hosts file using environment variable interpolation
                sh '''
                    mkdir -p ansible
                    cat > ansible/hosts << EOF
[coinxcel_servers]
${EC2_HOST} ansible_user=ubuntu ansible_ssh_private_key_file=/tmp/ec2_key.pem ansible_ssh_common_args='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o IdentitiesOnly=yes'

[coinxcel_servers:vars]
ansible_python_interpreter=/usr/bin/python3
ansible_become=yes
ansible_become_method=sudo
EOF
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Deploy to EC2') {
            steps {
                script {
                    // Debug EC2 host information
                    sh 'echo "EC2_HOST value: $EC2_HOST"'
                    
                    // Copy SSH key to a temporary location
                    withCredentials([sshUserPrivateKey(credentialsId: env.SSH_KEY_CREDENTIALS, keyFileVariable: 'SSH_KEY')]) {
                        sh 'mkdir -p /tmp'
                        sh 'cp $SSH_KEY /tmp/ec2_key.pem'
                        sh 'chmod 600 /tmp/ec2_key.pem'
                        
                        // Verify the hosts file content after creation
                        sh 'cat ansible/hosts'
                        
                        // Test SSH connectivity to EC2 instance
                        sh '''
                            echo "Testing SSH connectivity to $EC2_HOST..."
                            ssh -i /tmp/ec2_key.pem -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ubuntu@$EC2_HOST 'echo "SSH connection successful"'
                        '''
                        
                        // Use Ansible to install Docker with verbose output for debugging
                        sh 'ANSIBLE_DEBUG=1 ansible-playbook -i ansible/hosts ansible/install-docker.yml -v'
                        
                        // Use Ansible to deploy the application with verbose output
                        withEnv([
                            "DOCKER_HUB_USER=${env.DOCKER_HUB_USER}",
                            "MYSQL_CREDENTIALS_USR=${env.MYSQL_CREDENTIALS_USR}",
                            "MYSQL_CREDENTIALS_PSW=${env.MYSQL_CREDENTIALS_PSW}"
                        ]) {
                            withCredentials([
                                string(credentialsId: 'dockerhub-credentials', variable: 'DOCKER_HUB_CREDS_PSW')
                            ]) {
                                sh 'ANSIBLE_DEBUG=1 ansible-playbook -i ansible/hosts ansible/ec2-deploy.yml -v'
                            }
                        }
                        
                        // Remove SSH key after deployment
                        sh 'rm -f /tmp/ec2_key.pem'
                    }
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
                // Clean workspace
                cleanWs()
            }
        }
    }
}