# Stage 1: Build Stage
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app

## Copy Maven config and pre-fetch dependencies
#COPY pom.xml .
#RUN mvn dependency:go-offline || echo "Dependency pre-fetch failed, continuing..."
#
## Copy source code and build the application
#COPY src ./src
#RUN mvn clean install -DskipTests
#RUN mvn clean package -DskipTests


# Copy the entire project (including the parent POM)
COPY . .

# Ensure dependencies are downloaded
RUN mvn clean install -DskipTests

# Build only the API Gateway module
WORKDIR /app/api-gateway
RUN mvn package -DskipTests

# Stage 2: Run Stage (Slim JDK for smaller image size)
FROM eclipse-temurin:22-jdk
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application's port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]