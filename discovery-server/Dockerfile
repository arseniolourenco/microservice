# Use the official OpenJDK 17 slim image
FROM openjdk:20-ea-1-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the generated JAR file into the container
COPY target/*.jar app.jar

# Expose the Eureka Server port
#EXPOSE 8761

# Set the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]