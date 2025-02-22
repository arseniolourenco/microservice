# Use an official OpenJDK 17 image as the base
FROM openjdk:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot application JAR file into the container
COPY target/*.jar app.jar

# Expose the default port used by the Spring Boot application
#EXPOSE 8080

# Define the default command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]