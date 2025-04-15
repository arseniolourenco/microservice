# Use the official Eclipse Temurin JDK image as the base image
FROM eclipse-temurin:22-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file to the container
COPY target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]