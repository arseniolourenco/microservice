# Use Amazon Corretto JDK 22
FROM amazoncorretto:22

# Copy the application JAR file to the container
COPY target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]