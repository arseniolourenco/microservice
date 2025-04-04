# Use Amazon Corretto JDK 22
FROM amazoncorretto:22-alpine

<<<<<<< HEAD
# Set working directory
WORKDIR /app
=======
# Copy the application JAR file to the container
COPY target/*.jar app.jar

>>>>>>> main

# Copy the JAR file from target directory
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]