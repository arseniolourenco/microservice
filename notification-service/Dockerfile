# Use an official OpenJDK runtime as a parent image
FROM amazoncorretto:22-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file to the container
COPY target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]