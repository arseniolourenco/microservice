# FROM eclipse-temurin:22-jre
FROM openjdk:22

# Copy the JAR file separately to maximize caching
COPY target/*.jar app.jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]