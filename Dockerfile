# Use the official Maven image to build the project
FROM maven:3.8.1-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the entire source code into the container
COPY src /app/src

# Package the application (this will create a JAR file)
RUN mvn clean package -DskipTests

# Use a smaller base image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/CoinXcel-0.0.1-SNAPSHOT.jar /app/CoinXcel.jar

# Expose the port on which your Spring Boot app will run (default is 8080)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/CoinXcel.jar"]
