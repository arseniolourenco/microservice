# Use an official OpenJDK as the base image
FROM openjdk:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot application JAR file to the container
COPY target/*.jar app.jar

# Expose the port the API Gateway will run on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app.jar"]