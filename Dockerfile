# Use official OpenJDK runtime as base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Install necessary utilities
RUN apt-get update && \
    apt-get install -y maven tree && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper files with verbose output
COPY .mvn/ ./.mvn/
COPY mvnw ./
COPY pom.xml ./

# Debug: list contents of copied files
RUN ls -la .mvn && \
    ls -la mvnw && \
    ls -la pom.xml

# Copy the source code
COPY src ./src

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/target/myportfolio-0.0.1-SNAPSHOT.jar"]
