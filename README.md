# CoinXcel

## Overview

CoinXcel is a Spring Boot-based cryptocurrency trading platform with a MySQL backend. It provides a secure API for cryptocurrency data management and user authentication.

## Features

- User authentication with JWT token
- Two-factor authentication support
- RESTful API for crypto data management
- Docker containerization for easy deployment
- CI/CD pipeline using Jenkins

## Tech Stack

- Java 17
- Spring Boot 3.4.0
- Spring Security
- Spring Data JPA
- MySQL 8.0
- Docker & Docker Compose
- Jenkins
- Ansible

## Project Structure

The project follows standard Spring Boot application structure:

- `Model`: Entity classes for database mapping
- `Repository`: Data access layer
- `Service`: Business logic
- `Controller`: REST endpoints
- `Config`: Application configuration including security

## Setup and Installation

### Prerequisites

- Java 17
- Maven
- Docker and Docker Compose
- MySQL 8.0 (or use the provided Docker container)

### Local Development

1. Clone the repository:

   ```bash
   git clone https://github.com/munsif-dev/CoinXcel.git
   cd CoinXcel
   ```

2. Build the application:

   ```bash
   ./mvnw clean package
   ```

3. Run with Docker Compose:

   ```bash
   docker-compose up -d
   ```

4. Access the application at http://localhost:8080

### Environment Variables

The application uses the following environment variables:

- `SPRING_DATASOURCE_URL`: JDBC URL for MySQL connection
- `SPRING_DATASOURCE_USERNAME`: MySQL username
- `SPRING_DATASOURCE_PASSWORD`: MySQL password

## API Endpoints

### Public Endpoints

- `GET /`: Home page
- `POST /auth/login`: User login
- `POST /auth/register`: User registration

### Protected Endpoints

- `GET /api`: Secure API endpoint (requires authentication)

## CI/CD Pipeline

The project uses Jenkins for continuous integration and deployment:

1. **Checkout**: Retrieves the source code from the repository
2. **Build**: Compiles and packages the application with Maven
3. **Set Up MySQL**: Starts a MySQL container for testing
4. **Build and Push Docker Image**: Creates and pushes the Docker image to Docker Hub
5. **Deploy to EC2**: Deploys the application to an EC2 instance using Ansible
6. **Tear Down MySQL**: Removes the MySQL container used for testing

### Deployment Process

The deployment to EC2 uses Ansible to:

1. Install Docker and Docker Compose on the EC2 instance
2. Set up the application directory
3. Copy necessary configuration files
4. Pull the latest Docker images
5. Start the application using Docker Compose

## Security

- JWT-based authentication
- Password security with encryption
- CORS configuration for API access
- Two-factor authentication support

## Development Notes

- Run `./mvnw spring-boot:run` for local development without Docker
- Access H2 console at `/h2-console` for development database inspection (when using H2)
- Use `docker-compose -f docker-compose.yml -f docker-compose.dev.yml up` for development-specific settings

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a pull request

## License

This project is licensed under the MIT License.
